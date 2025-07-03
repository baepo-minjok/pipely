package com.example.backend.jenkins.error.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.error.model.dto.FailedBuildResDto;
import com.example.backend.jenkins.error.model.dto.FailedBuildSummaryResDto;
import com.example.backend.jenkins.error.model.dto.JobSummaryReqDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.service.FreeStyleJobService;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorService {
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final FreeStyleJobService freeStyleJobService;
    private final HttpClientService httpClientService;
    private final LlmService llmService;

    private final int maxRetryCount = 3;
    private final int retryIntervalSeconds = 120;

    public JenkinsInfo getJenkinsInfoByIdAndUser(UUID infoId, UUID userId) {
        return jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
    }

    public FreeStyle getVerifiedJob(UUID jobId, UUID userId) {
        FreeStyle job = freeStyleJobService.getFreeStyleById(jobId);

        if (!job.getJenkinsInfo().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return job;
    }



    public FailedBuildResDto getRecentBuild(JenkinsInfo info, String jobName) {
        String url = info.getUri() + "/job/" + jobName + "/lastBuild/api/json";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> lastBuild = httpClientService.exchange(url, HttpMethod.GET, entity, Map.class);

        if (lastBuild == null || !lastBuild.containsKey("result")) {
            throw new CustomException(ErrorCode.JENKINS_BUILD_INFO_MISSING);
        }

        return FailedBuildResDto.of(
                jobName,
                (Integer) lastBuild.get("number"),
                (String) lastBuild.get("result"),
                ((Number) lastBuild.get("timestamp")).longValue(),
                ((Number) lastBuild.get("duration")).longValue()
        );
    }

    public FailedBuildResDto getRecentBuildByJob(UUID jobId, UUID userId) {
        FreeStyle job = getVerifiedJob(jobId, userId);
        return getRecentBuild(job.getJenkinsInfo(), job.getJobName());
    }


    public List<FailedBuildResDto> getBuildsForJob(JenkinsInfo info, String jobName) {
        String url = info.getUri() + "/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> jobInfo = httpClientService.exchange(url, HttpMethod.GET, entity, Map.class);

        if (jobInfo == null || !jobInfo.containsKey("builds")) {
            throw new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND);
        }

        List<Map<String, Object>> buildList = (List<Map<String, Object>>) jobInfo.get("builds");
        List<FailedBuildResDto> builds = new ArrayList<>();
        for (Map<String, Object> build : buildList) {
            String result = (String) build.get("result");
            builds.add(FailedBuildResDto.of(
                    jobName,
                    (Integer) build.get("number"),
                    result != null ? result : "UNKNOWN",
                    ((Number) build.get("timestamp")).longValue(),
                    ((Number) build.get("duration")).longValue()
            ));
        }
        return builds;
    }

    public List<FailedBuildResDto> getBuildsForJobByUser(UUID jobId, UUID userId) {
        FreeStyle job = getVerifiedJob(jobId, userId);
        return getBuildsForJob(job.getJenkinsInfo(), job.getJobName());
    }


    public List<FailedBuildResDto> getFailedBuildsForJob(JenkinsInfo info, String jobName) {
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

        List<FailedBuildResDto> builds = new ArrayList<>();
        for (Map<String, Object> build : buildList) {
            String result = (String) build.get("result");
            if ("FAILURE".equals(result)) {
                builds.add(FailedBuildResDto.of(
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

    public List<FailedBuildResDto> getFailedBuildsForJobByUser(UUID jobId, UUID userId) {
        FreeStyle job = getVerifiedJob(jobId, userId); // 사용자 소유 확인 포함
        return getFailedBuildsForJob(job.getJenkinsInfo(), job.getJobName());
    }


    public FailedBuildSummaryResDto summarizeBuild(JenkinsInfo info, String jobName, int buildNumber) {
        String url = info.getUri() + "/job/" + jobName + "/" + buildNumber + "/consoleText";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.TEXT_PLAIN));
        String log = httpClientService.exchange(url, HttpMethod.GET, entity, String.class);

        if (!log.contains("Exception") && !log.contains("FAILURE") && !log.contains("Caused by")) {
            return FailedBuildSummaryResDto.builder()
                    .jobName(jobName)
                    .buildNumber(buildNumber)
                    .naturalResponse("이 빌드는 에러 없이 정상적으로 완료된 것으로 보입니다.")
                    .build();
        }

        String response = llmService.summarizeBuildLog(log);

        return FailedBuildSummaryResDto.builder()
                .jobName(jobName)
                .buildNumber(buildNumber)
                .naturalResponse(response)
                .build();
    }

    public FailedBuildSummaryResDto summarizeBuildByJob(JobSummaryReqDto dto, UUID userId) {
        FreeStyle job = getVerifiedJob(dto.getJobId(), userId);
        return summarizeBuild(job.getJenkinsInfo(), job.getJobName(), dto.getBuildNumber());
    }


    public void retryBuildIfFailed(JenkinsInfo info, String jobName, int buildNumber, int retryCount) {
        if (retryCount >= maxRetryCount) {
            log.warn("[RETRY] 최대 재시도 횟수 도달 - 중단 (job: {}, build: {})", jobName, buildNumber);
            return;
        }

        String url = info.getUri() + "/job/" + jobName + "/" + buildNumber + "/api/json";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> buildInfo = httpClientService.exchange(url, HttpMethod.GET, entity, Map.class);
        String result = (String) buildInfo.get("result");

        if (result == null) {
            log.info("[RETRY] 빌드 중 상태 감지 - {}초 후 재확인 (job: {}, build: {}, retry: {})",
                    retryIntervalSeconds, jobName, buildNumber, retryCount);
            sleep();
            retryBuildIfFailed(info, jobName, buildNumber, retryCount);
        } else if ("FAILURE".equalsIgnoreCase(result)) {
            log.warn("[RETRY] 빌드 실패 감지 - {}초 후 재시도 예정 (job: {}, build: {}, retry: {})",
                    retryIntervalSeconds, jobName, buildNumber, retryCount + 1);
            sleep();
            String triggerUrl = info.getUri() + "/job/" + jobName + "/build";
            HttpEntity<?> triggerEntity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
            httpClientService.exchange(triggerUrl, HttpMethod.POST, triggerEntity, String.class);
            retryBuildIfFailed(info, jobName, buildNumber + 1, retryCount + 1);
        } else {
            log.info("[RETRY] 빌드 성공 - 재시도 종료 (job: {}, build: {})", jobName, buildNumber);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(retryIntervalSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[RETRY] 재시도 대기 중 인터럽트 발생", e);
        }
    }

    public List<FailedBuildResDto> getRecentBuilds(JenkinsInfo info) {
        List<FailedBuildResDto> builds = new ArrayList<>();

        String jobListUrl = info.getUri() + "/api/json?tree=jobs[name]";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
        Map<?, ?> jobsResponse = httpClientService.exchange(jobListUrl, HttpMethod.GET, entity, Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        if (jobs == null || jobs.isEmpty()) return builds;

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");
            try {
                builds.add(getRecentBuild(info, jobName));
            } catch (CustomException e) {
                log.warn("[SKIP] {}: {}", jobName, e.getMessage());
            }
        }
        return builds;
    }

    public List<FailedBuildResDto> getFailedBuilds(JenkinsInfo info) {
        List<FailedBuildResDto> failedBuilds = new ArrayList<>();

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
}
