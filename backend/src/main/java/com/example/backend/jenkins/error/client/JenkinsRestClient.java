package com.example.backend.jenkins.error.client;

import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

public class JenkinsRestClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String username;
    private final String token;

    public JenkinsRestClient(String baseUrl, String username, String token) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.username = username;
        this.token = token;
    }

    public <T> T get(String endpoint, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, entity, responseType).getBody();
    }
}