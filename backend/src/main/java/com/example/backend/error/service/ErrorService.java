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

                String result = (String) lastBuild.get("result");
                if ("FAILURE".equals(result)) {
                    int buildNumber = (Integer) lastBuild.get("number");
                    long timestampMillis = ((Number) lastBuild.get("timestamp")).longValue();
                    long durationMillis = ((Number) lastBuild.get("duration")).longValue();

                    failedBuilds.add(FailedBuildDto.of(
                            jobName,
                            buildNumber,
                            result,
                            timestampMillis,
                            durationMillis
                    ));
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
                long timestampMillis = ((Number) lastBuild.get("timestamp")).longValue();
                long durationMillis = ((Number) lastBuild.get("duration")).longValue();
                // 실패, 성공, UNSTABLE, ABORTED 등 모두 포함
                if (result != null) {
                    builds.add(FailedBuildDto.of(
                            jobName,
                            (Integer) lastBuild.get("number"),
                            result,
                            timestampMillis,
                            durationMillis
                    ));
                }

            } catch (Exception e) {
                // 개별 Job 오류 무시
            }
        }

        return builds;
    }

}