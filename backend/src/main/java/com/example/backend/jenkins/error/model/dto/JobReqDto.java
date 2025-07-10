package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Job ID (파이프라인)", example = "d07c6f08-4ac7-45c5-a7dd-2b0ad91e0a50")
    private UUID jobId;

    @Schema(description = "Job 이름", example = "build-backend")
    private String jobName;
}
