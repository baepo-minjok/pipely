package com.example.backend.jenkins.error.model.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class JobReqDto {
    private UUID jobId; // 지금은 프리스타일 job id
    private String jobName;
}
