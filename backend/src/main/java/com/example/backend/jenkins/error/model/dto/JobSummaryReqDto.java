package com.example.backend.jenkins.error.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JobSummaryReqDto {
    private UUID jobId;
    private int buildNumber;;
}
