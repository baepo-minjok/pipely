package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Schema(
            description = "Job ID (파이프라인)",
            example = "d07c6f08-4ac7-45c5-a7dd-2b0ad91e0a50"
    )
    @NotNull(message = "jobId는 필수입니다.")
    private UUID jobId;

    @Schema(description = "Job 이름", example = "build-backend")
    @NotBlank(message = "jobName은 비어 있을 수 없습니다.")
    private String jobName;
}
