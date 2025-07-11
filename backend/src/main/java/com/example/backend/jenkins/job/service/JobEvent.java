package com.example.backend.jenkins.job.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

public class JobEvent {
    @Getter
    @AllArgsConstructor
    public static class JobCreatedEvent {
        private final UUID pipelineId;
        private final String config;
        private final String url;

    }

    @Getter
    @AllArgsConstructor
    public static class JobUpdatedEvent {
        private final UUID pipelineId;
        private final String config;
        private final String url;

    }
}
