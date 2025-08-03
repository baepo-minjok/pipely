package com.example.backend.jenkins.error.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.build.service.BuildService;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.JobSummaryDto;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto.FailedBuild;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto.FailedBuildSummary;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.jenkins.job.service.VersionService;
import com.example.backend.service.HttpClientService;
import jakarta.transaction.Transactional;
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
    private final VersionService versionService;
    private final PipelineService pipelineService;
    private final BuildService buildService;


    public List<FailedBuild> getFailedBuildsForJobByUser(UUID jobId, UUID userId) {
        Pipeline job = getVerifiedJobWithPipeline(jobId, userId); // 사용자 소유 확인 포함
        return getFailedBuildsForJob(job.getJenkinsInfo(), job.getName());
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


    public JenkinsInfo getJenkinsInfoByIdAndUser(UUID infoId, UUID userId) {
        return jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
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

    public Pipeline getVerifiedJobWithPipeline(UUID pipelineId, UUID userId) {
        Pipeline job = pipelineService.getPipelineById(pipelineId);

        if (!job.getJenkinsInfo().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return job;
    }

    public FailedBuildSummary summarizeBuildByJob(JobSummaryDto dto, UUID userId) {
        Pipeline job = getVerifiedJobWithPipeline(dto.getJobId(), userId);
        return summarizeBuild(job.getJenkinsInfo(), job.getName(), dto.getBuildNumber());
    }



    @Transactional
    public void rollbackToLastSuccessfulVersion(UUID pipelineId, UUID userId) {
        Pipeline pipeline = getVerifiedJobWithPipeline(pipelineId, userId);

        // BuildService 통해 마지막 성공 빌드 가져오기
        BuildResponseDto.BuildInfo lastSuccess = buildService.getBuildHistory(pipelineId).stream()
                .filter(b -> "SUCCESS".equalsIgnoreCase(b.getStatus()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_BUILD_INFO_MISSING));

        // 현재 latest 제외한 이전 PipelineVersion 중 가장 최근 찾기
        UUID latestId = pipeline.getLatestVersionId();
        List<PipelineVersion> versions = pipeline.getVersionList();

        PipelineVersion target = versions.stream()
                .filter(v -> !v.getId().equals(latestId))
                .max(Comparator.comparing(PipelineVersion::getCreatedAt))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_NO_SUCCESSFUL_BUILD));

        // rollbackToSnapshot 재사용
        versionService.rollbackToSnapshot(target.getId());
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

}
