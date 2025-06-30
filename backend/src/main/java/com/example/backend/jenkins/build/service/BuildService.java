package com.example.backend.jenkins.build.service;

import com.example.backend.jenkins.build.config.XmlConfigParser;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.build.model.dto.*;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.model.dto.BuildLogResponseDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.build.model.dto.BuildStreamLogResponseDto;
import com.example.backend.jenkins.build.model.dto.BuildTriggerRequestDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final JenkinsInfoService jenkinsInfoService;


    private final RestTemplate restTemplate;

    public ResponseEntity<?> getBuildInfo(String jobName , JobType jobType,UUID freeStyle) {
        log.info("빌드 정보 요청 - jobName: {}, jobType: {}", jobName, jobType);
        try {
            return switch (jobType) {
                case LATEST -> ResponseEntity.ok(getLastBuildStatus(jobName,freeStyle));
                case HISTORY -> ResponseEntity.ok(getBuildHistory(jobName,freeStyle));
            };
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("빌드 정보 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_SERVER_ERROR);
        }
    }

    public void triggerJenkinsBuild(BuildTriggerRequestDto requestDto, UUID freeStyle) {



        InfoResponseDto.DetailInfoDto info =  jenkinsInfoService.getDetailInfoById(freeStyle);
        String triggerUrl = info.getUri() + "/job/" + requestDto.getJobName() + "/buildWithParameters";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("STEP", String.valueOf(requestDto.getBuildTriggerType()));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(triggerUrl, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.error("빌드 트리거 실패 - jobName: {}", requestDto.getJobName(), e);
            throw new CustomException(ErrorCode.JENKINS_BUILD_TRIGGER_FAILED);
        }
    }

    public List<BuildResponseDto.BuildInfo> getBuildHistory(String job, UUID freeStyle) {
        ResponseEntity<String> response = JenkinsGetResponse(job,freeStyle);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response.getBody(), Map.class);
            return BuildResponseDto.BuildInfo.listFrom(body);
        } catch (JsonProcessingException e) {
            log.error("빌드 이력 JSON 파싱 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_BUILD_HISTORY_PARSE_ERROR);
        }
    }

    public BuildResponseDto.BuildInfo getLastBuildStatus(String job,UUID freeStyle) {
        ResponseEntity<String> response = JenkinsGetResponse(job,freeStyle);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response.getBody(), Map.class);
            return BuildResponseDto.BuildInfo.latestFrom(body);
        } catch (JsonProcessingException e) {
            log.error("최신 빌드 JSON 파싱 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_LATEST_BUILD_PARSE_ERROR);
        }
    }

    public BuildLogResponseDto.BuildLogDto getBuildLog(String jobName, String buildNumber, UUID freeStyle) {

        InfoResponseDto.DetailInfoDto info =  jenkinsInfoService.getDetailInfoById(freeStyle);

        String url = info.getUri() + "/job/" + jobName + "/" + buildNumber + "/console";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            Document doc = Jsoup.parse(response.getBody());
            Element pre = doc.selectFirst("pre.console-output");
            return BuildLogResponseDto.BuildLogDto.getLog(pre);
        } catch (Exception e) {
            log.error("콘솔 로그 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_CONSOLE_LOG_PARSE_ERROR);
        }
    }

    public BuildStreamLogResponseDto.BuildStreamLogDto getStreamLog(String jobName, String buildNumber, UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info =  jenkinsInfoService.getDetailInfoById(freeStyle);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(info.getUri() + "/job/" + jobName + "/" + buildNumber + "/logText/progressiveText")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return BuildStreamLogResponseDto.BuildStreamLogDto.getStreamLog(response.getBody());
        } catch (Exception e) {
            log.error("스트리밍 로그 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_STREAM_LOG_FAILED);
        }
    }

    public ResponseEntity<String> JenkinsGetResponse(String job,UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info =  jenkinsInfoService.getDetailInfoById(freeStyle);

        String url = info.getUri() + "/job/" + job + "/api/json"
                + "?tree=builds[number,result,timestamp,duration,building,id,url,actions[causes[userId,userName]]]";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());

        try {
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (Exception e) {
            log.error("Jenkins API 호출 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_API_CALL_FAILED);
        }
    }

    public String getSchedule(String jobName,UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info =  jenkinsInfoService.getDetailInfoById(freeStyle);

        String url = info.getUri() + "/job/" + jobName + "/config.xml";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("config.xml 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_CONFIG_XML_FETCH_FAILED);
        }
    }

    public String setSchedule(String jobName, String cron,UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info =  jenkinsInfoService.getDetailInfoById(freeStyle);

        try {
            String originalXml = getSchedule(jobName,freeStyle);
            String updatedXml = XmlConfigParser.updateCronSpecInXml(originalXml, cron);

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());
            headers.setContentType(MediaType.APPLICATION_XML);

            ResponseEntity<String> response = restTemplate.exchange(
                    info.getUri() + "/job/" + jobName + "/config.xml",
                    HttpMethod.POST,
                    new HttpEntity<>(updatedXml, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new CustomException(ErrorCode.JENKINS_CONFIG_XML_UPDATE_FAILED);
            }

            String newConfigXml = getSchedule(jobName,freeStyle);
            return XmlConfigParser.getCronSpecFromConfig(newConfigXml);

        } catch (CustomException ce) {

            throw ce;
        } catch (Exception e) {
            log.error("스케줄 설정 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_XML_CRON_PARSE_ERROR);
        }
    }


 }
