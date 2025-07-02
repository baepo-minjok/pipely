package com.example.backend.jenkins.error.client;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.HttpClientService;
import org.springframework.http.HttpEntity;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.springframework.http.RequestEntity.post;

public class JenkinsRestClient {

    private final Logger log = LoggerFactory.getLogger(JenkinsRestClient.class);
    private final HttpClientService httpClientService;
    private final String baseUrl;
    private final String username;
    private final String token;

    public JenkinsRestClient(String baseUrl, String username, String token, HttpClientService httpClientService) {
        this.baseUrl = removeTrailingSlash(baseUrl);
        this.username = username;
        this.token = token;
        this.httpClientService = httpClientService;
    }

    public <T> T get(String endpoint, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);

        return httpClientService.exchange(fullUrl, HttpMethod.GET, entity, responseType);
    }

    public void post(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        // Jenkins CSRF Token 헤더 필요 여부 체크
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);

        try {
            httpClientService.exchange(fullUrl, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.error("[Jenkins POST] 요청 실패: URL={}, 이유={}", fullUrl, e.getMessage(), e);
            throw new CustomException(ErrorCode.JENKINS_CONNECTION_FAILED);
        }
    }


    private String removeTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
    // 실패한 빌드 로그 조회 (Jenkins API)
    public String getConsoleLog(String jobName, int buildNumber) {
        String endpoint = String.format("/job/%s/%d/consoleText", jobName, buildNumber);
        return get(endpoint, String.class);
    }

    // 빌드 결과 조회
    public String getBuildResult(String jobName, int buildNumber) {
        String endpoint = String.format("/job/%s/%d/api/json", jobName, buildNumber);
        Map<?, ?> response = get(endpoint, Map.class);
        return (String) response.get("result"); // "SUCCESS", "FAILURE", null
    }

    // 빌드 실행 트리거
    public void triggerBuild(String jobName) {
        String endpoint = String.format("/job/%s/build", jobName);
        post(endpoint);
    }
}