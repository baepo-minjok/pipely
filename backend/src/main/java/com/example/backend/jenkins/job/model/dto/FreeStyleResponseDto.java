package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.FreeStyleHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class FreeStyleResponseDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "LightFreeStyleDto", description = "간략한 FreeStyle 응답 DTO")
    public static class LightFreeStyleDto {

        @Schema(
                description = "FreeStyle 고유 식별자 (UUID)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        private UUID id;

        @Schema(
                description = "Freestyle 잡 이름 (Jenkins 내에서의 Job 식별자)",
                example = "build-and-deploy",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String jobName;

        public static LightFreeStyleDto fromEntity(FreeStyle freeStyle) {
            return LightFreeStyleDto.builder()
                    .id(freeStyle.getId())
                    .jobName(freeStyle.getJobName())
                    .build();
        }
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "DetailFreeStyleDto", description = "상세 FreeStyle 정보 응답 DTO")
    public static class DetailFreeStyleDto {

        @Schema(
                description = "FreeStyle 고유 식별자 (UUID)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        private UUID id;

        @NotBlank
        @Schema(
                description = "Freestyle 잡 이름 (Jenkins 내에서의 Job 식별자)",
                example = "build-and-deploy"
        )
        private String jobName;

        @Schema(
                description = "Freestyle 잡 설명",
                example = "이 잡은 프로젝트를 빌드하고 배포합니다."
        )
        private String description;

        @Schema(
                description = "프로젝트 관련 URL",
                example = "https://github.com/example/project"
        )
        private String projectUrl;

        @Schema(
                description = "Jenkins UI에 표시될 프로젝트 이름",
                example = "Example Project"
        )
        private String projectDisplayName;

        @Schema(
                description = "GitHub Webhook 트리거 활성화 여부",
                example = "true"
        )
        private Boolean githubTrigger;

        @Schema(
                description = "빌드 대상 Git 저장소 URL",
                example = "https://github.com/example/project.git"
        )
        private String repositoryUrl;

        @Schema(
                description = "빌드 대상 Git 브랜치",
                example = "main"
        )
        private String branch;

        @Schema(
                description = "FreeStyle shell script 내용",
                example = "#!/bin/bash\n\necho \"Build started\"\n./gradlew build\n"
        )
        private String script;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        public static DetailFreeStyleDto toDetailFreeStyleDto(FreeStyle freeStyle) {
            return DetailFreeStyleDto.builder()
                    .id(freeStyle.getId())
                    .jobName(freeStyle.getJobName())
                    .description(freeStyle.getDescription())
                    .projectUrl(freeStyle.getProjectUrl())
                    .projectDisplayName(freeStyle.getProjectDisplayName())
                    .githubTrigger(freeStyle.getGithubTrigger())
                    .repositoryUrl(freeStyle.getRepositoryUrl())
                    .branch(freeStyle.getBranch())
                    .script(freeStyle.getScript())
                    .createdAt(freeStyle.getCreatedAt())
                    .updatedAt(freeStyle.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "LightHistoryDto", description = "간략한 버전 인덱스 응답 DTO")
    public static class LightHistoryDto {

        @Schema(
                description = "FreeStyleHistory 고유 식별자 (UUID)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        private UUID id;

        @Schema(
                description = "버전 인덱스",
                example = "3"
        )
        private int version;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "DetailHistoryDto", description = "상세 FreeStyleHistory 정보 응답 DTO")
    public static class DetailHistoryDto {

        @Schema(
                description = "FreeStyleHistory 고유 식별자 (UUID)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        private UUID id;

        @NotBlank
        @Schema(
                description = "Freestyle 잡 이름 (Jenkins 내에서의 Job 식별자)",
                example = "build-and-deploy"
        )
        private String jobName;

        @Schema(
                description = "Freestyle 잡 설명",
                example = "이 잡은 프로젝트를 빌드하고 배포합니다."
        )
        private String description;

        @Schema(
                description = "프로젝트 관련 URL",
                example = "https://github.com/example/project"
        )
        private String projectUrl;

        @Schema(
                description = "Jenkins UI에 표시될 프로젝트 이름",
                example = "Example Project"
        )
        private String projectDisplayName;

        @Schema(
                description = "GitHub Webhook 트리거 활성화 여부",
                example = "true"
        )
        private Boolean githubTrigger;

        @Schema(
                description = "빌드 대상 Git 저장소 URL",
                example = "https://github.com/example/project.git"
        )
        private String repositoryUrl;

        @Schema(
                description = "빌드 대상 Git 브랜치",
                example = "main"
        )
        private String branch;

        @Schema(
                description = "FreeStyle shell script 내용",
                example = "#!/bin/bash\n\necho \"Build started\"\n./gradlew build\n"
        )
        private String script;

        @Schema(
                description = "Jenkins 잡 구성 XML (config.xml) 내용",
                example = "<project>...</project>"
        )
        private String config;

        @Schema(
                description = "버전 인덱스",
                example = "3"
        )
        private Integer version;

        @Schema(
                description = "이력 생성 일시 (ISO-8601 형식)",
                example = "2025-07-03T12:00:00"
        )
        private LocalDateTime createdAt;

        public static DetailHistoryDto toDetailHistoryDto(FreeStyleHistory freeStyleHistory) {
            return DetailHistoryDto.builder()
                    .id(freeStyleHistory.getId())
                    .jobName(freeStyleHistory.getJobName())
                    .description(freeStyleHistory.getDescription())
                    .projectUrl(freeStyleHistory.getProjectUrl())
                    .projectDisplayName(freeStyleHistory.getProjectDisplayName())
                    .githubTrigger(freeStyleHistory.getGithubTrigger())
                    .repositoryUrl(freeStyleHistory.getRepositoryUrl())
                    .branch(freeStyleHistory.getBranch())
                    .script(freeStyleHistory.getScript())
                    .config(freeStyleHistory.getConfig())
                    .version(freeStyleHistory.getVersion())
                    .createdAt(freeStyleHistory.getCreatedAt())
                    .build();
        }
    }

}
