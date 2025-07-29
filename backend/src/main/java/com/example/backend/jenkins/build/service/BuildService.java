package com.example.backend.jenkins.build.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.parser.XmlConfigParser;
import com.example.backend.service.HttpClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final HttpClientService httpClientService;
    private final PipelineService pipelineService;
    private final XmlConfigParser xmlConfigParser;

    /**
     * Jenkins 파이프라인의 특정 스테이지 실행을 트리거한다.
     *
     * @param dto 실행할 스테이지 맵 및 파이프라인 ID
     */
    public int StageJenkinsBuild(BuildRequestDto.BuildStageRequestDto dto) {
        Pipeline pipeline = pipelineService.getPipelineById(dto.getJobId());
        pipelineService.setStatusPending(pipeline);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String triggerUrl = info.getUri() + "/job/" + pipeline.getName() + "/buildWithParameters";
        log.info("Jenkins Trigger URL = {}", triggerUrl);
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        for (String stage : dto.getStageBuilds()) {
            String paramKey = "RUN_" + stage.toUpperCase().replace(" ", "_");
            body.add(paramKey, "false");
        }
        body.add("ID", dto.getJobId().toString());
        String response = httpClientService.exchange(triggerUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        log.info("Jenkins 응답 상태: {}", response);
        return getBuildNumber(pipeline, info);
    }

    /**
     * 전체 빌드 이력을 조회한다.
     *
     * @param pipelineId 파이프라인 UUID
     * @return 빌드 정보 리스트
     */
    public List<BuildResponseDto.BuildInfo> getBuildHistory(UUID pipelineId) {
        String response = JenkinsGetResponse(pipelineId);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.listFrom(body);
        } catch (JsonProcessingException e) {
            log.error("빌드 이력 JSON 파싱 실패 - jobName: {}", e);
            throw new CustomException(ErrorCode.JENKINS_BUILD_HISTORY_PARSE_ERROR);
        }
    }

    /**
     * 최신 빌드 정보 1건을 반환한다.
     *
     * @param pipelineId 파이프라인 UUID
     * @return 최신 빌드 정보
     */
    public BuildResponseDto.BuildInfo getLastBuildStatus(UUID pipelineId) {
        String response = JenkinsGetResponse(pipelineId);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.latestFrom(body);
        } catch (JsonProcessingException e) {
            log.error("최신 빌드 JSON 파싱 실패 - jobName: {}", e);
            throw new CustomException(ErrorCode.JENKINS_LATEST_BUILD_PARSE_ERROR);
        }
    }

    /**
     * 빌드 번호 기준으로 Jenkins 콘솔 전체 로그를 조회한다.
     *
     * @param dto 빌드 번호, 파이프라인 UUID 포함
     * @return 로그 응답 DTO
     */
    public BuildResponseDto.BuildLogDto getBuildLog(BuildRequestDto.GetLogRequestDto dto) {
        Pipeline pipeline = pipelineService.getPipelineById(dto.getJobId());
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String url = info.getUri() + "/job/" + pipeline.getName() + "/" + dto.getBuildNumber() + "/console";
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);
        try {
            String response = httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            Document doc = Jsoup.parse(response);
            Element pre = doc.selectFirst("pre.console-output");
            return BuildResponseDto.BuildLogDto.getLog(pre);
        } catch (Exception e) {
            log.error("콘솔 로그 조회 실패 - jobName: {}", pipeline.getName(), e);
            throw new CustomException(ErrorCode.JENKINS_CONSOLE_LOG_PARSE_ERROR);
        }
    }

    /**
     * 실시간 빌드 로그(progressiveText)를 조회한다.
     *
     * @param jobId 파이프라인 UUID
     * @return 실시간 로그 DTO
     */
    public BuildResponseDto.BuildStreamLogDto getStreamLog(UUID jobId) {
        Pipeline pipeline = pipelineService.getPipelineById(jobId);
        JenkinsInfo info = pipeline.getJenkinsInfo();

        String lastBuildUri = info.getUri() + "/job/" + pipeline.getName() + "/lastBuild/buildNumber";
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String lastBuildResponse = httpClientService.exchange(
                lastBuildUri, HttpMethod.GET, entity, String.class
        );
        int lastBuildNumber = Integer.parseInt(lastBuildResponse.trim());
        URI logUri = UriComponentsBuilder
                .fromHttpUrl(info.getUri() + "/job/" + pipeline.getName() + "/" + lastBuildNumber + "/logText/progressiveText")
                .build().toUri();
        HttpHeaders logHeaders = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);
        String logResponse = httpClientService.exchange(logUri.toString(), HttpMethod.GET, new HttpEntity<>(logHeaders), String.class);
        return BuildResponseDto.BuildStreamLogDto.getStreamLog(logResponse);
    }

    /**
     * Jenkins 파이프라인 빌드 정보 API를 호출한다.
     *
     * @param pipelineId 파이프라인 UUID
     * @return Jenkins JSON Raw String
     */
    public String JenkinsGetResponse(UUID pipelineId) {
        Pipeline pipeline = pipelineService.getPipelineById(pipelineId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String url = info.getUri() + "/job/" + pipeline.getName() + "/api/json"
                + "?tree=builds[number,result,timestamp,duration,building,id,url,actions[causes[userId,userName]]]";
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);
        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    /**
     * 파이프라인(Jenkins Job)에 등록된 스테이지 목록을 추출한다.
     *
     * @param jobId 파이프라인 UUID
     * @return Stage DTO(스테이지 이름 리스트)
     */
    public BuildResponseDto.Stage getJobPipelineStage(UUID jobId) {
        Pipeline pipeline = pipelineService.getPipelineById(jobId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_XML);
        String xml = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/config.xml",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        List<String> stageNames = xmlConfigParser.getPipelineStageNamesFromXml(xml);
        return new BuildResponseDto.Stage(stageNames);
    }

    public int getBuildNumber(Pipeline pipeline, JenkinsInfo info) {
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON);

        Map<String, Object> json = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/api/json",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        return (int) json.get("nextBuildNumber");
        //Map<String, Object> lastBuild = (Map<String, Object>) json.get("lastBuild");
        //  return (int) lastBuild.get("number");
    }

    public int getDuration(UUID jobId, int buildNumber) {
        Pipeline pipeline = pipelineService.getPipelineById(jobId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON);

        Map<String, Object> json = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/" + buildNumber + "/api/json",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // 빌드 완료된 경우 -> 100%
        if (json.get("result") != null) {
            return 100;
        }

        // Jenkins API 값 추출
        Number estimatedNum = (Number) json.get("estimatedDuration");
        Number timestampNum = (Number) json.get("timestamp");

        if (estimatedNum == null || estimatedNum.longValue() <= 0 || timestampNum == null) {
            return 0; // 계산 불가 시 0%
        }

        long estimated = estimatedNum.longValue();
        long timestamp = timestampNum.longValue();
        long elapsed = System.currentTimeMillis() - timestamp;

        // 진행률 계산
        int progress = (int) ((elapsed / (double) estimated) * 100);
        return Math.min(progress, 99); // 진행 중은 99%까지만
    }

}
