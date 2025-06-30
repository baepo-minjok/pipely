package com.example.backend.jenkins.error.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.error.client.JenkinsRestClient;
import com.example.backend.jenkins.error.model.dto.FailedBuildResDto;
import com.example.backend.jenkins.error.client.JenkinsRestClient;
import com.example.backend.jenkins.error.model.dto.FailedBuildResDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ErrorService {
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private static final Logger log = LoggerFactory.getLogger(ErrorService.class);

    // 특정 Job의 최근 빌드 1건 조회
    public FailedBuildResDto getRecentBuild(JenkinsRestClient client, String jobName) {
        Map<?, ?> lastBuild = client.get("/job/" + jobName + "/lastBuild/api/json", Map.class);

        if (lastBuild == null || !lastBuild.containsKey("result")) {
            throw new CustomException(ErrorCode.JENKINS_BUILD_INFO_MISSING);
        }

        String result = (String) lastBuild.get("result");

        if (result != null) {
            return FailedBuildResDto.of(
                    jobName,
                    (Integer) lastBuild.get("number"),
                    result,
                    ((Number) lastBuild.get("timestamp")).longValue(),
                    ((Number) lastBuild.get("duration")).longValue()
            );
        }

        return null;
    }

    // 특정 Job의 전체 빌드 내역 조회 (성공/실패 모두 포함)
    public List<FailedBuildResDto> getBuildsForJob(JenkinsRestClient client, String jobName) {
        Map<?, ?> jobInfo = client.get("/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]", Map.class);
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

    // 특정 Job의 실패한 빌드 내역만 조회
    public List<FailedBuildResDto> getFailedBuildsForJob(JenkinsRestClient client, String jobName) {

        Map<?, ?> jobInfo = client.get("/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]", Map.class);

        if (jobInfo == null || !jobInfo.containsKey("builds")) {
            throw new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND);
        }

        List<Map<String, Object>> buildList = (List<Map<String, Object>>) jobInfo.get("builds");
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

    // 전체 Job 목록을 순회하며 최근 빌드 1건씩 조회
    public List<FailedBuildResDto> getRecentBuilds(JenkinsRestClient client) {
        List<FailedBuildResDto> builds = new ArrayList<>();

        Map<?, ?> jobsResponse = client.get("/api/json?tree=jobs[name,url]", Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        if (jobs == null || jobs.isEmpty()) {
            log.warn("Jenkins 서버에 등록된 Job이 없습니다.");
            return builds;
        }

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");

            try {
                Map<?, ?> lastBuild = client.get("/job/" + jobName + "/lastBuild/api/json", Map.class);

                if (lastBuild == null || !lastBuild.containsKey("result")) {
                    log.warn("Job [{}]의 최근 빌드 정보가 존재하지 않음 → 스킵", jobName);
                    continue;
                }

                String result = (String) lastBuild.get("result");

                builds.add(FailedBuildResDto.of(
                        jobName,
                        (Integer) lastBuild.get("number"),
                        result,
                        ((Number) lastBuild.get("timestamp")).longValue(),
                        ((Number) lastBuild.get("duration")).longValue()
                ));
            } catch (CustomException e) {
                log.warn("Job [{}] 처리 중 예외 발생 → {}: {}", jobName, e.getErrorCode().name(), e.getMessage());
                continue;
            }
        }

        return builds;
    }

    // 전체 Job 목록을 순회하며 최근 빌드가 실패한 Job만 필터링
    public List<FailedBuildResDto> getFailedBuilds(JenkinsRestClient client) {
        List<FailedBuildResDto> builds = new ArrayList<>();

        Map<?, ?> jobsResponse = client.get("/api/json?tree=jobs[name]", Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        if (jobs == null || jobs.isEmpty()) {
            throw new CustomException(ErrorCode.JENKINS_NO_JOBS_FOUND);
        }

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");

            try {
                Map<?, ?> jobInfo = client.get("/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]", Map.class);

                if (jobInfo == null || !jobInfo.containsKey("builds")) {
                    log.warn("Job [{}]의 빌드 목록을 가져올 수 없음 → 스킵", jobName);
                    continue;
                }

                List<Map<String, Object>> buildList = (List<Map<String, Object>>) jobInfo.get("builds");

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
            } catch (CustomException e) {
                log.warn("Job [{}] 처리 중 예외 발생 → {}: {}", jobName, e.getErrorCode().name(), e.getMessage());
                continue;
            }
        }

        if (builds.isEmpty()) {
            throw new CustomException(ErrorCode.JENKINS_ALL_JOBS_FAILED);
        }

        return builds;
    }

    public InfoResponseDto.DetailInfoDto getDetailInfoByIdAndUser(UUID infoId, UUID userId) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        return InfoResponseDto.DetailInfoDto.fromEntity(info);
    }


}
