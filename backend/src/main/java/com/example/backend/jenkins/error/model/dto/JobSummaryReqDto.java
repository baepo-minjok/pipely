package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JobSummaryReqDto {

    @Schema(description = "Job ID", example = "d07c6f08-4ac7-45c5-a7dd-2b0ad91e0a50")
    private UUID jobId;

    @Schema(description = "빌드 번호", example = "15")
    private int buildNumber;
}
