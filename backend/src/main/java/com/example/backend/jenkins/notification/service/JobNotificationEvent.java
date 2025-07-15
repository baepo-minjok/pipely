package com.example.backend.jenkins.notification.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class JobNotificationEvent<T> {
    private final UUID pipelineId;
    private final String jenkinsUrl;
    private final HttpMethod method;
    private final HttpEntity<T> requestEntity;
    private final Class<T> responseType;
}