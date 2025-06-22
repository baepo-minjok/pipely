package com.example.backend.build.service;


import com.example.backend.build.model.dto.BuildResponseDto;
import com.example.backend.build.model.dto.BuildQueryRequestDto;
import com.example.backend.build.model.JobType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final RestTemplate restTemplate;

    public ResponseEntity<?> getBuildInfo(BuildQueryRequestDto req) {
        JobType jobType = req.getJobType();
        log.info("빌드 정보 요청 - jobName: {}, jobType: {}", req.getJobName(), jobType);

        try {
            return switch (jobType) {
                case LATEST -> {
                    log.info("최신 빌드 상태 조회 실행");
                    yield ResponseEntity.ok(getLastBuildStatus(req.getJobName()));
                }
                case HISTORY -> {
                    log.info("빌드 이력 조회 실행");
                    yield ResponseEntity.ok(getBuildHistory(req.getJobName()));
                }
            };
        } catch (Exception e) {
            log.error("빌드 정보 조회 실패 - jobType: {}, jobName: {}", jobType, req.getJobName(), e);
            return ResponseEntity.internalServerError().body("서버 내부 오류 발생");
        }
    }

    public void triggerJenkinsBuild() {
        String jenkinsUrl = "http://15.164.104.2:8080";
        String jobName = "woojin_test";
        String username = "admin";
        String apiToken = "114cf329b1dbcc4e92dfca3c0d46f3c980";

        String triggerUrl = jenkinsUrl + "/job/" + jobName + "/build";
        log.info("Jenkins 빌드 트리거 - URL: {}", triggerUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(triggerUrl, HttpMethod.POST, entity, String.class);
            log.info("빌드 트리거 응답 상태: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("빌드 트리거 실패 - jobName: {}", jobName, e);
        }
    }

    public List<BuildResponseDto.BuildInfo> getBuildHistory(String job) {
        log.info("빌드 이력 조회 요청 - jobName: {}", job);

        ResponseEntity<String> response = JenkinsGetResponse(job);

        try {
            Map<String, Object> body = new ObjectMapper().readValue(response.getBody(), Map.class);
            log.info("빌드 이력 파싱 성공 - 빌드 개수: {}", ((List<?>) body.get("builds")).size());
            return BuildResponseDto.BuildInfo.listFrom(body);
        } catch (JsonProcessingException e) {
            log.error("빌드 이력 JSON 파싱 실패 - jobName: {}", job, e);
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    public BuildResponseDto.BuildInfo getLastBuildStatus(String job) {
        log.info("최신 빌드 상태 조회 - jobName: {}", job);

        ResponseEntity<String> response = JenkinsGetResponse(job);

        try {
            Map<String, Object> body = new ObjectMapper().readValue(response.getBody(), Map.class);
            log.info("최신 빌드 정보 파싱 성공");
            return BuildResponseDto.BuildInfo.latestFrom(body);
        } catch (JsonProcessingException e) {
            log.error("최신 빌드 JSON 파싱 실패 - jobName: {}", job, e);
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    public ResponseEntity<String> JenkinsGetResponse(String job) {
        String jenkinsUrl = "http://15.164.104.2:8080";
        String jobName = job;
        String username = "admin";
        String apiToken = "114cf329b1dbcc4e92dfca3c0d46f3c980";

        String triggerUrl = jenkinsUrl + "/job/" + jobName + "/api/json"
                + "?tree=builds[number,result,timestamp,duration,building,id,url,actions[causes[userId,userName]]]";

        log.info(" Jenkins API 호출 - URL: {}", triggerUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(triggerUrl, HttpMethod.GET, entity, String.class);
            log.info("Jenkins API 응답 수신 완료 - Status: {}", response.getStatusCode());
            return response;
        } catch (Exception e) {
            log.error("Jenkins API 호출 실패 - jobName: {}", job, e);
            throw new RuntimeException("Jenkins API 호출 실패", e);
        }
    }

    public String getBuildLog(String jobName, int buildNumber) {
        log.warn("getBuildLog() 아직 구현되지 않음 - jobName: {}, buildNumber: {}", jobName, buildNumber);
        return null;
    }
}
