package com.example.backend.jenkins.error.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobReqDto {
    private UUID jobId; // 지금은 프리스타일 job id
    private String jobName;
}
