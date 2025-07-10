package com.example.backend.jenkins.error.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.error.model.dto.*;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.JobSummaryDto;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.JobDto;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.RetryDto;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.JenkinsInfoDto;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto.FailedBuild;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto.FailedBuildSummary;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorService {
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final HttpClientService httpClientService;
    private final LlmService llmService;

    private final int maxRetryCount = 3;
    private final int retryIntervalSeconds = 120;
    private final PipelineRepository pipelineRepository;
    private final PipelineService pipelineService;

    public JenkinsInfo getJenkinsInfoByIdAndUser(UUID infoId, UUID userId) {
        return jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
    }
  
    public Pipeline getVerifiedJobWithPipeline(UUID pipelineId, UUID userId) {
        Pipeline job = pipelineService.getPipelineById(pipelineId);

        if (!job.getJenkinsInfo().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return job;
    }


    public ErrorResponseDto.FailedBuild getRecentBuild(JenkinsInfo info, String jobName) {
        String url = info.getUri() + "/job/" + jobName + "/lastBuild/api/json";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> lastBuild = httpClientService.exchange(url, HttpMethod.GET, entity, Map.class);

        if (lastBuild == null || !lastBuild.containsKey("result")) {
            throw new CustomException(ErrorCode.JENKINS_BUILD_INFO_MISSING);
        }

        return ErrorResponseDto.FailedBuild.of(
                jobName,
                (Integer) lastBuild.get("number"),
                (String) lastBuild.get("result"),
                ((Number) lastBuild.get("timestamp")).longValue(),
                ((Number) lastBuild.get("duration")).longValue()
        );
    }

    public ErrorResponseDto.FailedBuild getRecentBuildByJob(UUID jobId, UUID userId) {
        Pipeline job = getVerifiedJobWithPipeline(jobId, userId);
        return getRecentBuild(job.getJenkinsInfo(), job.getJobName());
    }


    public List<ErrorResponseDto.FailedBuild> getBuildsForJob(JenkinsInfo info, String jobName) {
        String url = info.getUri() + "/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> jobInfo = httpClientService.exchange(url, HttpMethod.GET, entity, Map.class);

        if (jobInfo == null || !jobInfo.containsKey("builds")) {
            throw new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND);
        }

        List<Map<String, Object>> buildList = (List<Map<String, Object>>) jobInfo.get("builds");
        List<ErrorResponseDto.FailedBuild> builds = new ArrayList<>();
        for (Map<String, Object> build : buildList) {
            String result = (String) build.get("result");
            builds.add(ErrorResponseDto.FailedBuild.of(
                    jobName,
                    (Integer) build.get("number"),
                    result != null ? result : "UNKNOWN",
                    ((Number) build.get("timestamp")).longValue(),
                    ((Number) build.get("duration")).longValue()
            ));
        }
        return builds;
    }

    public List<ErrorResponseDto.FailedBuild> getBuildsForJobByUser(UUID jobId, UUID userId) {
        Pipeline job = getVerifiedJobWithPipeline(jobId, userId);
        return getBuildsForJob(job.getJenkinsInfo(), job.getJobName());
    }


    public List<ErrorResponseDto.FailedBuild> getFailedBuildsForJob(JenkinsInfo info, String jobName) {
        String url = info.getUri() + "/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> jobInfo = httpClientService.exchange(url, HttpMethod.GET, entity, Map.class);

        if (jobInfo == null || !jobInfo.containsKey("builds")) {
            throw new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND);
        }

        List<Map<String, Object>> buildList = (List<Map<String, Object>>) jobInfo.get("builds");

        if (buildList == null || buildList.isEmpty()) {
            throw new CustomException(ErrorCode.JENKINS_BUILD_INFO_MISSING); // 빌드 자체가 없는 경우
        }

        List<ErrorResponseDto.FailedBuild> builds = new ArrayList<>();
        for (Map<String, Object> build : buildList) {
            String result = (String) build.get("result");
            if ("FAILURE".equals(result)) {
                builds.add(ErrorResponseDto.FailedBuild.of(
                        jobName,
                        (Integer) build.get("number"),
                        result,
                        ((Number) build.get("timestamp")).longValue(),
                        ((Number) build.get("duration")).longValue()
                ));
            }
        }
        return builds;
    }

    public List<FailedBuild> getFailedBuildsForJobByUser(UUID jobId, UUID userId) {
        Pipeline job = getVerifiedJobWithPipeline(jobId, userId); // 사용자 소유 확인 포함
        return getFailedBuildsForJob(job.getJenkinsInfo(), job.getJobName());
    }


    public FailedBuildSummary summarizeBuild(JenkinsInfo info, String jobName, int buildNumber) {
        String url = info.getUri() + "/job/" + jobName + "/" + buildNumber + "/consoleText";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.TEXT_PLAIN));
        String log = httpClientService.exchange(url, HttpMethod.GET, entity, String.class);

        if (!log.contains("Exception") && !log.contains("FAILURE") && !log.contains("Caused by")) {
            return FailedBuildSummary.builder()
                    .jobName(jobName)
                    .buildNumber(buildNumber)
                    .naturalResponse("이 빌드는 에러 없이 정상적으로 완료된 것으로 보입니다.")
                    .build();
        }

        String response = llmService.summarizeBuildLog(log);

        return FailedBuildSummary.builder()
                .jobName(jobName)
                .buildNumber(buildNumber)
                .naturalResponse(response)
                .build();
    }

    public FailedBuildSummary summarizeBuildByJob(JobSummaryDto dto, UUID userId) {
        Pipeline job = getVerifiedJobWithPipeline(dto.getJobId(), userId);
        return summarizeBuild(job.getJenkinsInfo(), job.getJobName(), dto.getBuildNumber());
    }


    public List<FailedBuild> getRecentBuilds(JenkinsInfo info) {
        List<FailedBuild> builds = new ArrayList<>();

        String jobListUrl = info.getUri() + "/api/json?tree=jobs[name]";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> jobsResponse = httpClientService.exchange(jobListUrl, HttpMethod.GET, entity, Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        if (jobs == null || jobs.isEmpty()) return builds;

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");

            try {
                FailedBuild build = getRecentBuild(info, jobName);
                if (build != null) {
                    builds.add(build);
                }
            } catch (CustomException e) {
                log.warn("[SKIP] {}: {}", jobName, e.getMessage());
            } catch (Exception e) {
                // 혹시 모를 예상치 못한 오류도 잡아서 무시하고 넘어가기
                log.warn("[SKIP] {}: 알 수 없는 예외 - {}", jobName, e.getMessage());
            }
        }
        return builds;
    }

    public List<FailedBuild> getFailedBuilds(JenkinsInfo info) {
        List<FailedBuild> failedBuilds = new ArrayList<>();

        String jobListUrl = info.getUri() + "/api/json?tree=jobs[name]";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> jobsResponse = httpClientService.exchange(jobListUrl, HttpMethod.GET, entity, Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        if (jobs == null || jobs.isEmpty()) return failedBuilds;

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");
            try {
                failedBuilds.addAll(getFailedBuildsForJob(info, jobName));
            } catch (CustomException e) {
                log.warn("Job [{}] 실패 이력 조회 중 예외 발생: {}", jobName, e.getMessage());
                continue;
            }
        }


        return failedBuilds;
    }

    private void applyJenkinsConfig(JenkinsInfo info, String jobName, String configXml) {
        String configUrl = info.getUri() + "/job/" + jobName + "/config.xml";
        HttpEntity<String> postReq = new HttpEntity<>(configXml, httpClientService.buildHeaders(info, MediaType.APPLICATION_XML));
        httpClientService.exchange(configUrl, HttpMethod.POST, postReq, String.class);
    }

    /*public void retryWithRollback(UUID pipelineId, UUID userId) {

        // 1. 유저 권한 검증
        Pipeline pipeline = getVerifiedJobWithPipeline(pipelineId, userId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String jobName = pipeline.getName();

        // 2. 최근 빌드 실패 여부 확인
        FailedBuild latestBuild = getRecentBuild(info, jobName);
        if (!"FAILURE".equals(latestBuild.getResult())) {
            throw new CustomException(ErrorCode.JENKINS_BUILD_NOT_FAILED);
        }

        // 3. 최근 성공 빌드 중 가장 마지막 빌드 찾기
        List<FailedBuild> history = getBuildsForJob(info, jobName);
        FailedBuild lastSuccess = history.stream()
                .filter(b -> "SUCCESS".equals(b.getResult()))
                .max(Comparator.comparing(FailedBuild::getBuildNumber))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_SUCCESS_BUILD_NOT_FOUND));

        // 4. 해당 빌드의 로그에서 version 추출
        String logUrl = info.getUri() + "/job/" + jobName + "/" + lastSuccess.getBuildNumber() + "/consoleText";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.TEXT_PLAIN));
        String buildLog = httpClientService.exchange(logUrl, HttpMethod.GET, entity, String.class);

        String versionStr = extractVersionFromLog(buildLog);
        if (versionStr == null) {
            throw new CustomException(ErrorCode.JENKINS_VERSION_NOT_FOUND_IN_LOG);
        }
        int version = Integer.parseInt(versionStr);

        // 5. 해당 version의 PipelineHistory 조회
        PipelineHistory rollbackHistory = pipelineHistoryRepository
                .findAllWithPipelineAndJenkinsInfoByPipelineIdAndVersion(pipelineId, version)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_PIPELINE_HISTORY_NOT_FOUND));

        log.info("[⏪ ROLLBACK CONFIG] jobName={}, version={}, config.xml=\n{}",
                jobName, version, rollbackHistory.getConfig());

        // 6. 설정 롤백 적용
        applyJenkinsConfig(info, jobName, rollbackHistory.getConfig());

        try {
            Thread.sleep(5000); // Jenkins가 config 반영할 시간
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 7. 빌드 재시도 트리거
        String triggerUrl = info.getUri() + "/job/" + jobName + "/build";
        httpClientService.exchange(triggerUrl, HttpMethod.POST, entity, String.class);
    }*/


}
