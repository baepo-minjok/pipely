package com.example.backend.error.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class JenkinsRestClient {

    private final RestTemplate restTemplate;

    @Value("${jenkins.base-url}")
    private String baseUrl;

    @Value("${jenkins.username}")
    private String username;

    @Value("${jenkins.token}")
    private String token;

    public <T> T get(String endpoint, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, entity, responseType).getBody();
    }
}
