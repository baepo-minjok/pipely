package com.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final RestTemplateBuilder restTemplateBuilder;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();

// 기본 에러 핸들러 보관
        ResponseErrorHandler defaultHandler = restTemplate.getErrorHandler();

// 커스텀 핸들러 정의: 401, 혹은 원하는 상태를 예외 없이 처리
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatusCode status = response.getStatusCode();
                // 예외 안 잡음
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // 401일 때는 이 메서드가 호출되지 않으므로, 기본 핸들러에 위임
                defaultHandler.handleError(response);
            }
        });
        return restTemplate;
    }
}
