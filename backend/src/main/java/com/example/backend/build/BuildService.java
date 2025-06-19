package com.example.backend.build;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BuildService {

    private final RestTemplate restTemplate;




    public void triggerJenkinsBuild(String branch, String env) {
        String jenkinsUrl = "http://15.164.104.2:8080";
        String jobName = "woojin_test";
        String username = "admin";
        String apiToken = "114cf329b1dbcc4e92dfca3c0d46f3c980";

        // 1. Crumb 발급
        String crumbUrl = jenkinsUrl + "/crumbIssuer/api/json";
        HttpHeaders crumbHeaders = new HttpHeaders();
        crumbHeaders.setBasicAuth(username, apiToken);
        HttpEntity<String> crumbRequest = new HttpEntity<>(crumbHeaders);

        ResponseEntity<Map> crumbResponse = restTemplate.exchange(
                crumbUrl, HttpMethod.GET, crumbRequest, Map.class
        );

        String crumb = (String) crumbResponse.getBody().get("crumb");
        String crumbField = (String) crumbResponse.getBody().get("crumbRequestField");

        // 2. Build 트리거
        String triggerUrl = jenkinsUrl + "/job/" + jobName + "/build";

//                "/buildWithParameters" +
//                "?branch=" + branch + "&env=" + env;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        headers.set(crumbField, crumb); // 예: "Jenkins-Crumb"

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                triggerUrl, HttpMethod.POST, entity, String.class
        );

        System.out.println("Build trigger status: " + response.getStatusCode());
    }

    public Object getBuildHistory(String jobName) {

        return null;


    }

    public String getBuildLog(String jobName, int buildNumber) {

        return null;

    }




    public LastBuildStatusResponse getLastBuildStatus(String job) {
        String jenkinsUrl = "http://15.164.104.2:8080";
        String jobName = job;
        String username = "admin";
        String apiToken = "114cf329b1dbcc4e92dfca3c0d46f3c980";

        // 1. Crumb 발급
        String crumbUrl = jenkinsUrl + "/crumbIssuer/api/json";
        HttpHeaders crumbHeaders = new HttpHeaders();
        crumbHeaders.setBasicAuth(username, apiToken);
        HttpEntity<String> crumbRequest = new HttpEntity<>(crumbHeaders);

        ResponseEntity<Map> crumbResponse = restTemplate.exchange(
                crumbUrl, HttpMethod.GET, crumbRequest, Map.class
        );

        String crumb = (String) crumbResponse.getBody().get("crumb");
        String crumbField = (String) crumbResponse.getBody().get("crumbRequestField");
        // 2. Build 트리거
        String triggerUrl = jenkinsUrl + "/job/" + jobName + "/lastBuild" + "/api/json";

//                "/buildWithParameters" +
//                "?branch=" + branch + "&env=" + env;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        headers.set(crumbField, crumb); // 예: "Jenkins-Crumb"

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                triggerUrl, HttpMethod.POST, entity, String.class
        );


        try {
            Map<String, Object> body = new ObjectMapper().readValue(response.getBody(), Map.class);
            return LastBuildStatusResponse.parseLastBuild(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }





    }
}
