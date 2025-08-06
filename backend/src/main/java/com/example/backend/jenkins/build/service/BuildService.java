package com.example.backend.jenkins.build.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.service.BuildPollingService;
import com.example.backend.service.HttpClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final HttpClientService httpClientService;
    private final PipelineService pipelineService;
    private final ObjectMapper objectMapper;
    private final BuildPollingService pollingManager;

    // ────────────────────────────────────────────────────────────────
    // 1. 파이프라인 트리거
    // ────────────────────────────────────────────────────────────────

    /**
     * Jenkins 파이프라인의 특정 스테이지 실행을 트리거한다.
     */
    public int triggerStages(BuildRequestDto.BuildStageRequestDto dto) {
        Pipeline pipeline = getPipeline(dto.getJobId());
        pipelineService.setStatusPending(pipeline);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String triggerUrl = info.getUri() + "/job/" + pipeline.getName() + "/buildWithParameters";
        log.info("Jenkins Trigger URL = {}", triggerUrl);

        MultiValueMap<String, String> body = buildStageTriggerParams(dto);
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        String response = httpClientService.exchange(triggerUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        log.info("Jenkins 응답 상태: {}", response);

        return getBuildNumber(pipeline, info);
    }

    private MultiValueMap<String, String> buildStageTriggerParams(BuildRequestDto.BuildStageRequestDto dto) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        dto.getStageBuilds().forEach(stage ->
                body.add("RUN_" + stage.toUpperCase().replace(" ", "_"), "false")
        );
        body.add("ID", dto.getJobId().toString());
        return body;
    }

    // ────────────────────────────────────────────────────────────────
    // 2. 빌드 이력/상태/로그 조회
    // ────────────────────────────────────────────────────────────────

    /**
     * 전체 빌드 이력을 조회한다.
     */
    public List<BuildResponseDto.BuildInfo> getBuildHistory(UUID pipelineId) {
        String response = getJenkinsJobJson(pipelineId);
        return parseBuildInfoList(response, pipelineId);
    }

    /**
     * 최신 빌드 정보 1건을 반환한다.
     */
    public BuildResponseDto.BuildInfo getLastBuildStatus(UUID pipelineId) {
        String response = getJenkinsJobJson(pipelineId);
        return parseLatestBuildInfo(response, pipelineId);
    }

    /**
     * 빌드 번호 기준으로 Jenkins 콘솔 전체 로그를 조회한다.
     */
    public BuildResponseDto.BuildLogDto getBuildLog(BuildRequestDto.GetLogRequestDto dto) {
        Pipeline pipeline = getPipeline(dto.getJobId());
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

    // ────────────────────────────────────────────────────────────────
    // 3. 내부 API & 변환 유틸
    // ────────────────────────────────────────────────────────────────

    private Pipeline getPipeline(UUID pipelineId) {
        return pipelineService.getPipelineById(pipelineId);
    }

    private String getJenkinsJobJson(UUID pipelineId) {
        Pipeline pipeline = getPipeline(pipelineId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String url = info.getUri() + "/job/" + pipeline.getName() + "/api/json"
                + "?tree=builds[number,result,timestamp,duration,building,id,url,actions[causes[userId,userName]]]";
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);
        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private List<BuildResponseDto.BuildInfo> parseBuildInfoList(String response, UUID pipelineId) {
        try {
            Map<String, Object> body = objectMapper.readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.listFrom(body);
        } catch (JsonProcessingException e) {
            log.error("빌드 이력 JSON 파싱 실패 - pipelineId: {}", pipelineId, e);
            throw new CustomException(ErrorCode.JENKINS_BUILD_HISTORY_PARSE_ERROR);
        }
    }

    private BuildResponseDto.BuildInfo parseLatestBuildInfo(String response, UUID pipelineId) {
        try {
            Map<String, Object> body = objectMapper.readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.latestFrom(body);
        } catch (JsonProcessingException e) {
            log.error("최신 빌드 JSON 파싱 실패 - pipelineId: {}", pipelineId, e);
            throw new CustomException(ErrorCode.JENKINS_LATEST_BUILD_PARSE_ERROR);
        }
    }

    private int fetchLastBuildNumber(JenkinsInfo info, String jobName) {
        String lastBuildUri = info.getUri() + "/job/" + jobName + "/lastBuild/buildNumber";
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON);
        String lastBuildResponse = httpClientService.exchange(lastBuildUri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        return Integer.parseInt(lastBuildResponse.trim());
    }

    // ────────────────────────────────────────────────────────────────
    // 4. 스테이지/진행률/상태 처리
    // ────────────────────────────────────────────────────────────────

    public String getJobPipelineStage(UUID jobId) {
        Pipeline pipeline = getPipeline(jobId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_XML);
        String xmlBody = """
                <jenkins>
                    <install plugin="pipeline-rest-api@latest"/>
                </jenkins>
                """;
        String url = info.getUri() + "/pluginManager/installNecessaryPlugins";
        return httpClientService.exchange(url, HttpMethod.POST, new HttpEntity<>(xmlBody, headers), String.class);
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
    }

    public Integer getCurrentBuildNumber(UUID jobId) {
        Pipeline pipeline = getPipeline(jobId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        return getBuildNumber(pipeline, info) - 1;
    }

    @Transactional(readOnly = true)
    public String getDuration(UUID jobId, int buildNumber) {
        Pipeline pipeline = getPipeline(jobId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON);

        Map<String, Object> json = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/" + buildNumber + "/api/json",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        String result = (String) json.get("result");
        if (result != null) {
            pipelineService.setState(pipeline, result);
            return result;
        }

        Number estimatedNum = (Number) json.get("estimatedDuration");
        Number timestampNum = (Number) json.get("timestamp");

        if (estimatedNum == null || estimatedNum.longValue() <= 0 || timestampNum == null) {
            return "0";
        }

        long estimated = estimatedNum.longValue();
        long timestamp = timestampNum.longValue();
        long elapsed = System.currentTimeMillis() - timestamp;
        int progress = (int) ((elapsed / (double) estimated) * 100);
        return String.valueOf(Math.min(progress, 99));
    }

    // ────────────────────────────────────────────────────────────────
    // 5. 빌드 상태, 중단 및 실시간 로그 전송
    // ────────────────────────────────────────────────────────────────

    public void stopBuild(UUID jobId) {
        Pipeline pipeline = getPipeline(jobId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON);

        int buildNumber = fetchLastBuildNumber(info, pipeline.getName());

        httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/" + buildNumber + "/stop",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class
        );
        pipelineService.setState(pipeline, "ABORTED");
    }

    public BuildResponseDto.BuildStatusDto viewBuild(UUID jobId, int buildNumber) {
        Pipeline pipeline = getPipeline(jobId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON);

        String log = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/" + buildNumber + "/consoleText",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        String json = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/" + buildNumber + "/wfapi/describe",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        String progress = getDuration(jobId, buildNumber);

        JsonNode root = parseJson(json, jobId, buildNumber);
        List<Map<String, String>> stageList = extractStages(root);

        String status = convertJenkinsStatus(root.path("status").asText());

        return BuildResponseDto.BuildStatusDto.builder()
                .id(jobId)
                .status(status)
                .stages(stageList)
                .log(log)
                .progress(progress)
                .build();
    }

    private JsonNode parseJson(String json, UUID jobId, int buildNumber) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("Jenkins wfapi/describe JSON 파싱 실패 - jobId: {}, buildNumber: {}", jobId, buildNumber, e);
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, String>> extractStages(JsonNode root) {
        List<Map<String, String>> stageList = new ArrayList<>();
        JsonNode stagesNode = root.get("stages");
        if (stagesNode != null && stagesNode.isArray()) {
            for (JsonNode stage : stagesNode) {
                Map<String, String> stageInfo = new HashMap<>();
                stageInfo.put("name", stage.path("name").asText());
                stageInfo.put("status", stage.path("status").asText());
                stageList.add(stageInfo);
            }
        }
        return stageList;
    }

    private String convertJenkinsStatus(String jenkinsStatus) {
        return switch (jenkinsStatus) {
            case "FAILED", "NOT_EXECUTED" -> "BUILD_FAILURE";
            case "ABORTED" -> "BUILD_ABORTED";
            case "SUCCESS" -> "BUILD_SUCCESS";
            default -> "BUILD_RUNNING";
        };
    }

    /**
     * 빌드 상태를 주기적으로 프론트로 전송 (실시간 진행률).
     */
    public void sendLog(UUID jobId, int buildNumber, String email) {
        pollingManager.sendLog(
                jobId, buildNumber, email,
                this::viewBuild // 기존 viewBuild()를 람다로 넘김
        );
    }
}
