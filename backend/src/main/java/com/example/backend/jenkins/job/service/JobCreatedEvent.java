package com.example.backend.jenkins.job.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class JobCreatedEvent {
    private final UUID pipelineId;
    private final String config;

}
