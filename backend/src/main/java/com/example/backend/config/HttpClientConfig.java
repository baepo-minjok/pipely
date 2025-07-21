package com.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfig {

    private final RestTemplateBuilder restTemplateBuilder;

    @Bean
    public RestTemplate restTemplate() {

        return restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }
}
