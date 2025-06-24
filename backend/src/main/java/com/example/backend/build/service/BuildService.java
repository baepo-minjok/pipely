package com.example.backend.build.service;


import com.example.backend.build.model.dto.*;
import com.example.backend.build.model.JobType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final RestTemplate restTemplate;


    // 빌드 내역 조회 최신,빌드 내역
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


    // job 빌드 트리거
    public void triggerJenkinsBuild(BuildTriggerRequestDto requestDto) {
        String jenkinsUrl = "http://15.164.104.2:8080";
        String jobName = requestDto.getJobName();
        String username = "admin";
        String apiToken = "114cf329b1dbcc4e92dfca3c0d46f3c980";




        String triggerUrl = jenkinsUrl + "/job/" + jobName + "/buildWithParameters";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // ★ 필수
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("STEP", String.valueOf(requestDto.getBuildTriggerType()));

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    triggerUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("✅ 빌드 트리거 성공 - 상태: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("❌ 빌드 트리거 실패 - jobName: {}", jobName, e);
        }
    }


    // 빌드 내역 조회
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


    // 최신 빌드 조회
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


    public BuildLogResponseDto.BuildLogDto getBuildLog(String jobName, String buildNumber) {


        String jenkinsUrl = "http://15.164.104.2:8080";
        String username = "admin";
        String apiToken = "114cf329b1dbcc4e92dfca3c0d46f3c980";

        String triggerUrl = jenkinsUrl + "/job/" + jobName + "/" + buildNumber + "/console";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(triggerUrl, HttpMethod.GET, entity, String.class);

            log.info("Jenkins API 응답 수신 완료 - Status: {}", response.getStatusCode());


            String html = response.getBody();
            Document doc = Jsoup.parse(html);
            Element pre = doc.selectFirst("pre.console-output");


            log.info("response: {}", pre);


            log.info("Jenkins API 응답 수신 완료 - Status: {}", response.getStatusCode());


            return BuildLogResponseDto.BuildLogDto.getLog(pre);


        } catch (Exception e) {
            log.error("Jenkins API 호출 실패 - jobName: {}", jobName, e);
            throw new RuntimeException("Jenkins API 호출 실패", e);
        }


    }



    public BuildStreamLogResponseDto.BuildStreamLogDto getStreamLog(String jobName, String buildNumber) {

        String jenkinsUrl = "http://15.164.104.2:8080";
        String username = "admin";
        String apiToken = "114cf329b1dbcc4e92dfca3c0d46f3c980";


        URI uri = UriComponentsBuilder
                .fromHttpUrl(jenkinsUrl + "/job/" + jobName + "/" + buildNumber + "/logText/progressiveText")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);






        return BuildStreamLogResponseDto.BuildStreamLogDto.getStreamLog(response.getBody());

    }






    // 젠킨스 빌드 내역 조회 url get
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

}
