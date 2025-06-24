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

    public List<FailedBuildResDto> getFailedBuilds(JenkinsRestClient client) {
        List<FailedBuildResDto> builds = new ArrayList<>();

        Map<?, ?> jobsResponse = client.get("/api/json?tree=jobs[name,url]", Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsResponse.get("jobs");

        for (Map<String, Object> job : jobs) {
            String jobName = (String) job.get("name");
            try {
                Map<?, ?> lastBuild = client.get("/job/" + jobName + "/lastBuild/api/json", Map.class);
                String result = (String) lastBuild.get("result");

                if ("FAILURE".equals(result)) {
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
}
