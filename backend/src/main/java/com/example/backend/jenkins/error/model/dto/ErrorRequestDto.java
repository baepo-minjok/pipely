package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class ErrorRequestDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JenkinsDto {

        @Schema(
                description = "JenkinsInfo Id",
                example = "47a00e4f-5da6-41dd-a54d-efb6676e9485"
        )
        @NotNull(message = "infoId는 필수입니다.")
        private UUID infoId;

        @Schema(
                description = "Job 이름",
                example = "build-api"
        )
        @NotBlank(message = "jobName은 비어 있을 수 없습니다.")
        private String jobName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobDto {

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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JenkinsInfoDto {

        @Schema(
                description = "JobInfo ID",
                example = "8e5a678a-a954-4b45-b0dd-8d3a323b07e"
        )
        @NotNull(message = "infoId는 필수입니다.")
        private UUID infoId;
    }

    @Getter
    @Builder
    public static class JobSummaryDto {

        @Schema(
                description = "Job ID",
                example = "d07c6f08-4ac7-45c5-a7dd-2b0ad91e0a50"
        )
        @NotNull(message = "jobId는 필수입니다.")
        private UUID jobId;

        @Schema(
                description = "빌드 번호",
                example = "15"
        )
        @Min(value = 1, message = "buildNumber는 1 이상이어야 합니다.")
        private int buildNumber;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RetryDto {

        @Schema(
                description = "Job ID (롤백 대상)",
                example = "d07c6f08-4ac7-45c5-a7dd-2b0ad91e0a50"
        )
        @NotNull(message = "jobId는 필수입니다.")
        private UUID jobId;
    }

}
