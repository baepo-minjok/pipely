package com.example.backend.jenkins.error.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.error.model.dto.FailedBuildResDto;
import com.example.backend.jenkins.error.model.dto.FailedBuildSummaryResDto;
import com.example.backend.jenkins.error.model.dto.JobSummaryReqDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.FreeStyleHistory;
import com.example.backend.jenkins.job.model.JobVersion;
import com.example.backend.jenkins.job.repository.FreeStyleHistoryRepository;
import com.example.backend.jenkins.job.repository.JobVersionRepository;
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
    private final JobVersionRepository jobVersionRepository;
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

    public void retryWithRollback(UUID jobId, UUID userId) {
        FreeStyle job = getVerifiedJob(jobId, userId);
        JenkinsInfo info = job.getJenkinsInfo();
        String jobName = job.getJobName();

        // 1. 최근 빌드 실패 여부 확인
        FailedBuildResDto latestBuild = getRecentBuild(info, jobName);
        if (!"FAILURE".equals(latestBuild.getResult())) {
            throw new CustomException(ErrorCode.JENKINS_BUILD_NOT_FAILED);
        }

        // 2. 최근 성공 빌드 찾기
        List<FailedBuildResDto> history = getBuildsForJob(info, jobName);
        FailedBuildResDto lastSuccess = history.stream()
                .filter(b -> "SUCCESS".equals(b.getResult()))
                .max(Comparator.comparing(FailedBuildResDto::getBuildNumber))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_SUCCESS_BUILD_NOT_FOUND));

        // 3. 해당 빌드의 로그에서 설정 버전 추출
        String logUrl = info.getUri() + "/job/" + jobName + "/" + lastSuccess.getBuildNumber() + "/consoleText";
        HttpEntity<?> entity = new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.TEXT_PLAIN));
        String log = httpClientService.exchange(logUrl, HttpMethod.GET, entity, String.class);

        String version = extractVersionFromLog(log);
        if (version == null) {
            throw new CustomException(ErrorCode.JENKINS_VERSION_NOT_FOUND_IN_LOG);
        }

        int versionNum = Integer.parseInt(version);  // ex. "1"

        // 4. JobVersion에서 해당 설정 가져오기
        JobVersion jobVersion = jobVersionRepository
                .findTopByJob_IdAndVersionOrderByVersionCreatedAtDesc(jobId, versionNum)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));

        // 5. 설정 롤백 적용 (Jenkins config.xml 업데이트)
        applyJenkinsConfig(info, jobName, jobVersion.getScriptContent());

        // 6. 빌드 재시도 트리거
        String triggerUrl = info.getUri() + "/job/" + jobName + "/build";
        httpClientService.exchange(triggerUrl, HttpMethod.POST, entity, String.class);
    }

    private String extractVersionFromLog(String log) {
        for (String line : log.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#VERSION:")) {
                String[] parts = trimmed.split(":");
                if (parts.length > 1) {
                    return parts[1].trim(); // ex. "2"
                }
            }
        }
        return null;
    }


    private void applyJenkinsConfig(JenkinsInfo info, String jobName, String configXml) {
        String configUrl = info.getUri() + "/job/" + jobName + "/config.xml";
        HttpEntity<String> postReq = new HttpEntity<>(configXml, httpClientService.buildHeaders(info, MediaType.APPLICATION_XML));
        httpClientService.exchange(configUrl, HttpMethod.POST, postReq, String.class);
    }




}
