package com.example.backend.jenkins.job.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.UUID;

public class JobEvent {
    @Getter
    @AllArgsConstructor
    public static class JobCreatedEvent<T> {
        private final UUID pipelineId;
        private final String url;
        private final HttpMethod method;
        private final HttpEntity<?> requestEntity;
        private final Class<T> responseType;
    }

    @Getter
    @AllArgsConstructor
    public static class JobUpdatedEvent<T> {
        private final UUID pipelineId;
        private final String url;
        private final HttpMethod method;
        private final HttpEntity<?> requestEntity;
        private final Class<T> responseType;
    }

    @Getter
    @AllArgsConstructor
    public static class JobDeletedEvent<T> {
        private final UUID pipelineId;
        private final String url;
        private final HttpMethod method;
        private final HttpEntity<?> requestEntity;
        private final Class<T> responseType;
    }
}
