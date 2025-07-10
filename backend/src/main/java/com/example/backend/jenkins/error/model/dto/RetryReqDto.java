package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetryReqDto {

    @Schema(description = "Job ID (롤백 대상)", example = "d07c6f08-4ac7-45c5-a7dd-2b0ad91e0a50")
    private UUID jobId;
}
