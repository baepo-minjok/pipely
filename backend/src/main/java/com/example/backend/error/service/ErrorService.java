package com.example.backend.error.service;

import com.example.backend.error.client.JenkinsRestClient;
import com.example.backend.error.model.dto.FailedBuildDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ErrorService {

    private final JenkinsRestClient jenkinsRestClient;

    public List<FailedBuildDto> getFailedBuilds() {
        List<FailedBuildDto> failedBuilds = new ArrayList<>();

        Map<?, ?> jobsResponse = jenkinsRestClient.get("/api/json?tree=jobs[name,url]", Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");
            try {
                Map<?, ?> lastBuild = jenkinsRestClient.get("/job/" + jobName + "/lastBuild/api/json", Map.class);
                if ("FAILURE".equals(lastBuild.get("result"))) {
                    failedBuilds.add(FailedBuildDto.builder()
                            .jobName(jobName)
                            .buildNumber((Integer) lastBuild.get("number"))
                            .result("FAILURE")
                            .timestamp(lastBuild.get("timestamp").toString())
                            .duration(lastBuild.get("duration").toString())
                            .build());
                }
            } catch (Exception e) {
                // 개별 Job 오류 무시
            }
        }

        return failedBuilds;
    }

    public List<FailedBuildDto> getRecentBuilds() {
        List<FailedBuildDto> builds = new ArrayList<>();

        Map<?, ?> jobsResponse = jenkinsRestClient.get("/api/json?tree=jobs[name,url]", Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");
            try {
                Map<?, ?> lastBuild = jenkinsRestClient.get("/job/" + jobName + "/lastBuild/api/json", Map.class);
                String result = (String) lastBuild.get("result");

                // 실패, 성공, UNSTABLE, ABORTED 등 모두 포함
                if (result != null) {
                    builds.add(FailedBuildDto.builder()
                            .jobName(jobName)
                            .buildNumber((Integer) lastBuild.get("number"))
                            .result(result)
                            .timestamp(lastBuild.get("timestamp").toString())
                            .duration(lastBuild.get("duration").toString())
                            .build());
                }

            } catch (Exception e) {
                // 개별 Job 오류 무시
            }
        }

        return builds;
    }

}