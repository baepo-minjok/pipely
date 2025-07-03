package com.example.backend.jenkins.error.model.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class JobSummaryReqDto {
    private UUID jobId;
    private int buildNumber;;
}
