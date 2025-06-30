package com.example.backend.jenkins.error.client;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.HttpClientService;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

public class JenkinsRestClient {

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
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);

        return httpClientService.exchange(fullUrl, HttpMethod.GET, entity, responseType);
    }


    private String removeTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
    // 실패한 빌드 로그 조회 (Jenkins API)
    public String getConsoleLog(String jobName, int buildNumber) {
        String endpoint = String.format("/job/%s/%d/consoleText", jobName, buildNumber);
        return get(endpoint, String.class);
    }
}