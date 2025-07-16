package com.example.backend.jenkins.notification.service;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JenkinsClientFactory {
    private final HttpClientService httpClientService;
    private final JenkinsInfoRepository jenkinsInfoRepository;


    public class JenkinsClient {
        private final WebClient webClient;

        public JenkinsClient(String url, String username, String apiToken) {
            this.webClient = WebClient.builder()
                    .baseUrl(url)
                    .defaultHeaders(headers -> headers.setBasicAuth(username, apiToken))
                    .build();
        }

        public Mono<Void> createGlobalCredential(String credentialId, String secret) {
            String jsonPayload = "{\n" +
                    "  \"\": \"0\",\n" +
                    "  \"credentials\": {\n" +
                    "    \"scope\": \"GLOBAL\",\n" +
                    "    \"id\": \"" + credentialId + "\",\n" +
                    "    \"description\": \"Webhook for pipeline\",\n" +
                    "    \"secret\": \"" + secret + "\",\n" +
                    "    \"$class\": \"org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl\"\n" +
                    "  }\n" +
                    "}";

            return webClient.post()
                    .uri("/credentials/store/system/domain/_/createCredentials")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData("json", jsonPayload))
                    .retrieve()
                    .bodyToMono(Void.class);
        }

        public Mono<Void> deleteGlobalCredential(String credentialId) {
            return webClient.post()
                    .uri("/credentials/store/system/domain/_/credential/{id}/doDelete", credentialId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData("Submit", "OK"))
                    .retrieve()
                    .bodyToMono(Void.class);
        }
    }

    public JenkinsClient createClientForUser(UUID userId) {
        JenkinsInfo info = jenkinsInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Jenkins info not found for user: " + userId));
        return new JenkinsClient(info.getUri(), info.getJenkinsId(), info.getApiToken());
    }


}