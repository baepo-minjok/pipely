package com.example.backend.jenkins.build.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.config.JobTriggerConfigurer;
import com.example.backend.jenkins.job.service.PipelineJobService;
import com.example.backend.parser.XmlConfigParser;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.service.FreeStyleJobService;
import com.example.backend.service.HttpClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final JenkinsInfoService jenkinsInfoService;
    private final HttpClientService httpClientService;
    private final FreeStyleJobService freeStyleJobService;
    private final JobTriggerConfigurer jobTriggerConfigurer;
    private final PipelineJobService pipelineJobService;
    private final XmlConfigParser  xmlConfigParser;

    public ResponseEntity<?> getBuildInfo(String jobName, JobType jobType, Map<String, String> JobStyleId) {
        log.info("빌드 정보 요청 - jobName: {}, jobType: {}", jobName, jobType);
        try {
            return switch (jobType) {
                case LATEST -> ResponseEntity.ok(getLastBuildStatus(jobName, JobStyleId));
                case HISTORY -> ResponseEntity.ok(getBuildHistory(jobName, JobStyleId));
            };
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("빌드 정보 조회 실패 - jobName: {}", jobName, e);
                throw new CustomException(ErrorCode.JENKINS_SERVER_ERROR);
        }
    }

    public void setStage(BuildRequestDto.StageSettingRequestDto req, Map<String, String> allParams) {
        JenkinsInfo info = getJobTypeJnekinsInfo(allParams);

        if (allParams.containsKey("freeStyle")) {
            jobTriggerConfigurer.setupFreestyleStage(req, info.getId());
        } else {
            stagePipeline(req, info.getId());
        }
    }


    /*
    * 특정 스테이지 실행 freestyle
    * */
    public void StageFreeStyleJenkinsBuild(BuildRequestDto.BuildStageRequestDto requestDto, Map<String, String> JobStyleId) {
        JenkinsInfo info = getJobTypeJnekinsInfo(JobStyleId);


        String triggerUrl = info.getUri() + "/job/" + requestDto.getJobName() + "/buildWithParameters";
        log.info("Jenkins Trigger URL = {}", triggerUrl);


        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        requestDto.getStageToggles().forEach((key, value) ->
                body.add("DO_" + key.toUpperCase(), String.valueOf(value)));
        httpClientService.exchange(triggerUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);


    }

    /*
    * 특정 job 빌드 내역 조회
    * */
    public List<BuildResponseDto.BuildInfo> getBuildHistory(String job, Map<String, String> JobStyleId) {
        String response = JenkinsGetResponse(job, JobStyleId);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.listFrom(body);
        } catch (JsonProcessingException e) {
            log.error("빌드 이력 JSON 파싱 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_BUILD_HISTORY_PARSE_ERROR);
        }
    }

    /*
    * 특정 job 의 마지막 build 번호 조회
    * */

    public BuildResponseDto.BuildInfo getLastBuildStatus(String job, Map<String, String> JobStyleId) {
        String response = JenkinsGetResponse(job, JobStyleId);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.latestFrom(body);
        } catch (JsonProcessingException e) {
            log.error("최신 빌드 JSON 파싱 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_LATEST_BUILD_PARSE_ERROR);
        }
    }

    /*
    * job 의 특정 빌드 번호의 빌드 로그 조회
    *
    * */
    public BuildResponseDto.BuildLogDto getBuildLog(String jobName, String buildNumber, Map<String, String> JobStyleId) {
        JenkinsInfo info = getJobTypeJnekinsInfo(JobStyleId);
        String url = info.getUri() + "/job/" + jobName + "/" + buildNumber + "/console";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        try {
            String response = httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            Document doc = Jsoup.parse(response);
            Element pre = doc.selectFirst("pre.console-output");
            return BuildResponseDto.BuildLogDto.getLog(pre);
        } catch (Exception e) {
            log.error("콘솔 로그 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_CONSOLE_LOG_PARSE_ERROR);
        }
    }

    /*
    * 특정 job의 실시간 빌드 조회
    *
    * */
    public BuildResponseDto.BuildStreamLogDto getStreamLog(String jobName, String buildNumber, Map<String, String> JobStyleId) {
        // TODO : build 번호 안받고 마지막 빌드 번호 조회하게 해서 동적 할당하기


        JenkinsInfo info = getJobTypeJnekinsInfo(JobStyleId);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(info.getUri() + "/job/" + jobName + "/" + buildNumber + "/logText/progressiveText")
                .build().toUri();

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        String response = httpClientService.exchange(uri.toString(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        return BuildResponseDto.BuildStreamLogDto.getStreamLog(response);
    }
    /*
    *
    * */
    public String JenkinsGetResponse(String job, Map<String, String> JobStyleId) {
        JenkinsInfo info = getJobTypeJnekinsInfo(JobStyleId);
        String url = info.getUri() + "/job/" + job + "/api/json"
                + "?tree=builds[number,result,timestamp,duration,building,id,url,actions[causes[userId,userName]]]";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
    /*
    * cron 시간 읽어옴
    * */

    public String getSchedule(String jobName, Map<String, String> JobStyleId) {
        JenkinsInfo info = getJobTypeJnekinsInfo(JobStyleId);
        String url = info.getUri() + "/job/" + jobName + "/config.xml";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }


    /*
     * cron 수정
     *
     * */

    public void setSchedule(BuildRequestDto.SetScheduleJob req) {
        Map<String, String> JobStyleId = req.getJobStyleId();
        String jobName = req.getJobName();
        String cron = req.getCron();

        JenkinsInfo info = getJobTypeJnekinsInfo(JobStyleId);
        String updatedXml = xmlConfigParser.updateCronSpecInXml(getSchedule(jobName, JobStyleId), cron);

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_XML);

        httpClientService.exchange(
                info.getUri() + "/job/" + jobName + "/config.xml",
                HttpMethod.POST,
                new HttpEntity<>(updatedXml, headers),
                String.class
        );


        xmlConfigParser.getCronSpecFromConfig(getSchedule(jobName, JobStyleId));
    }






    public void stagePipeline(BuildRequestDto.StageSettingRequestDto req, UUID id) {
        // TODO: 파이프라인 트리거 로직 구현 필요
    }







    public BuildResponseDto.Stage getJobPipelineStage(String jobname, Map<String, String> jobStyleId) {

        JenkinsInfo info = getJobTypeJnekinsInfo(jobStyleId);





        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_XML);

        String xml = httpClientService.exchange(
                info.getUri() + "/job/" + jobname + "/config.xml",
                HttpMethod.GET,
                new HttpEntity<>(headers)    ,
                String.class
        );
        List<String> stageNames =  xmlConfigParser.getPipelineStageNamesFromXml(xml);
        return new BuildResponseDto.Stage(stageNames);


    }

    public JenkinsInfo getJobTypeJnekinsInfo(Map<String, String> JobStyleId){
        if (JobStyleId.containsKey("freeStyle")) {
            UUID id = UUID.fromString(JobStyleId.get("freeStyle"));
            return freeStyleJobService.getJenkinsInfoByFreeStyleId(id);
        } else if (JobStyleId.containsKey("pipeLine")) {
            UUID id = UUID.fromString(JobStyleId.get("pipeLine"));
            return pipelineJobService.getJenkinsInfoByFreeStyleId(id);
        } else {
            throw new CustomException(ErrorCode.JENKINS_JOB_TYPE_FAILED);
        }
    }
}


