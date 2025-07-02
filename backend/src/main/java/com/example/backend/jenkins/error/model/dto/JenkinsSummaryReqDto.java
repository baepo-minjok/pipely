package com.example.backend.jenkins.error.model.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class JenkinsSummaryReqDto {
    private UUID infoId;
    private String jobName;
    private int buildNumber;
}
