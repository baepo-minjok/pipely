package com.example.backend.jenkins.error.client;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

public class JenkinsRestClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String username;
    private final String token;

    public JenkinsRestClient(String baseUrl, String username, String token) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = removeTrailingSlash(baseUrl);
        this.username = username;
        this.token = token;
    }

    public <T> T get(String endpoint, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);

            ResponseEntity<T> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, responseType);

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            // 404 → Job 없음
            throw new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND);
        } catch (HttpClientErrorException e) {
            // 401, 403, 400 등 → 클라이언트 오류
            throw new CustomException(ErrorCode.JENKINS_API_CALL_FAILED);
        } catch (RestClientException e) {
            // 커넥션 오류 등
            throw new CustomException(ErrorCode.JENKINS_API_CALL_FAILED);
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
}