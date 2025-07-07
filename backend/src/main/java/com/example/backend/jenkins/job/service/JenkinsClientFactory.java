package com.example.backend.jenkins.job.service;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JenkinsClientFactory {

    private final JenkinsInfoRepository jenkinsInfoRepository;

    public class JenkinsClient {
        private final WebClient webClient;

        public JenkinsClient(String url, String username, String apiToken) {
            String basicAuth = buildBasicAuthHeader(username, apiToken);

            this.webClient = WebClient.builder()
                    .baseUrl(url)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth)
                    .build();
        }

        private String buildBasicAuthHeader(String username, String apiToken) {
            String auth = username + ":" + apiToken;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            return "Basic " + new String(encodedAuth);
        }

        public Mono<Void> createGlobalCredential(String credentialId, String secret) {
            String jsonPayload = "{\n" +
                    "  \"\": \"0\",\n" +
                    "  \"credentials\": {\n" +
                    "    \"scope\": \"GLOBAL\",\n" +
                    "    \"id\": \"" + credentialId + "\",\n" +
                    "    \"description\": \"Webhook for freestyle job\",\n" +
                    "    \"secret\": \"" + secret + "\",\n" +
                    "    \"$class\": \"org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl\"\n" +
                    "  }\n" +
                    "}";

            log.debug("[JenkinsClient] 요청 jsonPayload: {}", jsonPayload);

            return webClient.post()
                    .uri("/credentials/store/system/domain/_/createCredentials")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData("json", jsonPayload))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                        log.error("[JenkinsClient] Jenkins API 에러 상태: {}", response.statusCode());
                        return response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("[JenkinsClient] 에러 바디: {}", errorBody);
                            return Mono.error(new RuntimeException("Jenkins API 오류: " + errorBody));
                        });
                    })
                    .bodyToMono(Void.class)
                    .doOnSuccess(unused -> log.info("[JenkinsClient] 크리덴셜 생성 성공: {}", credentialId))
                    .doOnError(error -> {
                        if (error instanceof WebClientResponseException) {
                            WebClientResponseException ex = (WebClientResponseException) error;
                            log.error("[JenkinsClient] WebClient 오류 상태: {}, 응답: {}",
                                    ex.getStatusCode(), ex.getResponseBodyAsString());
                        } else {
                            log.error("[JenkinsClient] WebClient 예외 발생", error);
                        }
                    });
        }

        public Mono<Void> runNotificationScript(String script) {
            return webClient.post()
                    .uri("/scriptText")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData("script", script))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                        log.error("[JenkinsClient] Jenkins 스크립트 실행 에러 상태: {}", response.statusCode());
                        return response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("[JenkinsClient] 에러 바디: {}", errorBody);
                            return Mono.error(new RuntimeException("스크립트 실행 오류: " + errorBody));
                        });
                    })
                    .bodyToMono(Void.class)
                    .doOnSuccess(unused -> log.info("[JenkinsClient] 알림 스크립트 실행 성공"))
                    .doOnError(error -> {
                        if (error instanceof WebClientResponseException ex) {
                            log.error("[JenkinsClient] WebClient 오류 상태: {}, 응답: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                        } else {
                            log.error("[JenkinsClient] WebClient 예외 발생", error);
                        }
                    });
        }
    }

    public JenkinsClient createClientForUser(UUID userId) {
        JenkinsInfo info = jenkinsInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Jenkins info not found for user: " + userId));

        return new JenkinsClient(info.getUri(), info.getJenkinsId(), info.getSecretKey());
    }
}