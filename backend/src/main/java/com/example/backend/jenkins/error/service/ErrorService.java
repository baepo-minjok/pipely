package com.example.backend.jenkins.error.service;

import com.example.backend.jenkins.error.client.JenkinsRestClient;
import com.example.backend.jenkins.error.model.dto.FailedBuildResDto;
import com.example.backend.jenkins.error.client.JenkinsRestClient;
import com.example.backend.jenkins.error.model.dto.FailedBuildResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ErrorService {

    // 특정 Job의 최근 빌드 1건 조회
    public FailedBuildResDto getRecentBuild(JenkinsRestClient client, String jobName) {
        try {
            Map<?, ?> lastBuild = client.get("/job/" + jobName + "/lastBuild/api/json", Map.class);
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
        } catch (Exception e) {
            // 무시 또는 로깅 가능
        }
        return null;
    }

    // 특정 Job의 전체 빌드 내역 조회 (성공/실패 모두 포함)
    public List<FailedBuildResDto> getBuildsForJob(JenkinsRestClient client, String jobName) {
        List<FailedBuildResDto> builds = new ArrayList<>();

        try {
            Map<?, ?> jobInfo = client.get("/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]", Map.class);
            List<Map<String, Object>> buildList = (List<Map<String, Object>>) jobInfo.get("builds");

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
        } catch (Exception e) {
            // 무시
        }

        return builds;
    }

    // 특정 Job의 실패한 빌드 내역만 조회
    public List<FailedBuildResDto> getFailedBuildsForJob(JenkinsRestClient client, String jobName) {
        List<FailedBuildResDto> builds = new ArrayList<>();

        try {
            Map<?, ?> jobInfo = client.get("/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]", Map.class);
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
        } catch (Exception e) {
            // 무시
        }

        return builds;
    }

    // 전체 Job 목록을 순회하며 최근 빌드 1건씩 조회
    public List<FailedBuildResDto> getRecentBuilds(JenkinsRestClient client) {
        List<FailedBuildResDto> builds = new ArrayList<>();

        Map<?, ?> jobsResponse = client.get("/api/json?tree=jobs[name,url]", Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");
            try {
                Map<?, ?> lastBuild = client.get("/job/" + jobName + "/lastBuild/api/json", Map.class);
                String result = (String) lastBuild.get("result");

                if (result != null) {
                    builds.add(FailedBuildResDto.of(
                            jobName,
                            (Integer) lastBuild.get("number"),
                            result,
                            ((Number) lastBuild.get("timestamp")).longValue(),
                            ((Number) lastBuild.get("duration")).longValue()
                    ));
                }

            } catch (Exception e) {
                // 개별 Job 오류 무시
            }
        }

        return builds;
    }

    // 전체 Job 목록을 순회하며 최근 빌드가 실패한 Job만 필터링
    public List<FailedBuildResDto> getFailedBuilds(JenkinsRestClient client) {
        List<FailedBuildResDto> builds = new ArrayList<>();

        Map<?, ?> jobsResponse = client.get("/api/json?tree=jobs[name]", Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");
            try {
                Map<?, ?> jobInfo = client.get("/job/" + jobName + "/api/json?tree=builds[number,result,timestamp,duration]", Map.class);
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

            } catch (Exception e) {
                // 무시
            }
        }

        return builds;
    }



}
