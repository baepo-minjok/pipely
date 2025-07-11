package com.example.backend.jenkins.build.model.dto;

import com.example.backend.jenkins.build.model.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BuildRequestDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "특정 스테이지 실행 요청 DTO")
    public static class BuildStageRequestDto {

        @Schema(description = "Jenkins Job 이름", example = "sample-job")
        private String jobName;

        @Schema(description = "스테이지 실행 여부 맵 (예: {\"TEST\": true})")
        private Map<String, Boolean> stageToggles;

        @Schema(description = "파이프라인 UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID pipelineId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "스테이지 설정 요청 DTO")
    public static class StageSettingRequestDto {

        @Schema(description = "Jenkins Job 이름", example = "sample-job")
        private String jobName;

        @Schema(description = "스테이지 목록", example = "[\"BUILD\", \"TEST\", \"DEPLOY\"]")
        private List<String> stage;

        @Schema(description = "파이프라인 UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID pipelineId;
    }





    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "스케줄 설정 요청 DTO")
    public static class SetScheduleJob {

        @Schema(description = "Jenkins Job 이름", example = "sample-job")
        private String jobName;

        @Schema(description = "CRON 표현식", example = "0 0 * * *")
        private String cron;

        @Schema(description = "파이프라인 UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID pipelineId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "빌드 로그 요청 DTO (스트리밍 포함)")
    public static class GetLogRequestDto {

        @Schema(description = "Jenkins Job 이름", example = "sample-job")
        private String jobName;

        @Schema(description = "빌드 번호", example = "42")
        private String buildNumber;

        @Schema(description = "파이프라인 UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID pipelineId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "스테이지 목록 조회 요청 DTO")
    public static class GetJobNameJobRequestDto {

        @Schema(description = "Jenkins Job 이름", example = "sample-job")
        private String jobName;

        @Schema(description = "파이프라인 UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID pipelineId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "빌드 이력 조회 요청 DTO")
    public static class getBuildHistory {

        @Schema(description = "Jenkins Job 이름", example = "sample-job")
        private String jobName;

        @Schema(description = "Job 유형", example = "LATEST")
        private JobType jobType;

        @Schema(description = "파이프라인 UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID pipelineId;
    }
}
