package com.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfig {

    private final RestTemplateBuilder restTemplateBuilder;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();

        // 기본 에러 핸들러 보관
        ResponseErrorHandler defaultHandler = restTemplate.getErrorHandler();

        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false; // 모든 상태에 대해 예외 발생시키지 않음
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // 아무 처리도 하지 않음
            }
        });


        return restTemplate;
    }
}
