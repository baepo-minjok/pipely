package com.example.backend.jenkins.job.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.UUID;

public class FreeStyleRequestDto {

    @Getter
    @Schema(name = "CreateFreeStyleDto", description = "새로운 FreeStyle 등록을 위한 요청 데이터")
    public static class CreateFreeStyleDto {

        @NotBlank
        @Schema(
                description = "JenkinsInfo 고유 식별자 (UUID)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private UUID infoId;

        @NotBlank
        @Schema(
                description = "Freestyle 잡 이름 (Jenkins 내에서의 Job 식별자)",
                example = "build-and-deploy",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String jobName;

        @Schema(
                description = "Freestyle 잡 설명",
                example = "이 잡은 프로젝트를 빌드하고 배포합니다.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String description;

        @Schema(
                description = "프로젝트 관련 URL",
                example = "https://github.com/example/project",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String projectUrl;

        @Schema(
                description = "Jenkins UI에 표시될 프로젝트 이름",
                example = "Example Project",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String projectDisplayName;

        @Schema(
                description = "GitHub Webhook 트리거 활성화 여부",
                example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private Boolean githubTrigger;

        @Schema(
                description = "빌드 대상 Git 저장소 URL",
                example = "https://github.com/example/project.git",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String repositoryUrl;

        @Schema(
                description = "빌드 대상 Git 브랜치",
                example = "main",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String branch;

        @Schema(
                description = "FreeStyle shell script 내용",
                example = "#!/bin/bash\n\necho \"Build started\"\n./gradlew build\n",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String script;
    }

    @Getter
    @Schema(name = "UpdateFreeStyleDto", description = "FreeStyle 수정을 위한 요청 데이터")
    public static class UpdateFreeStyleDto {

        @NotBlank
        @Schema(
                description = "FreeStyle 고유 식별자 (UUID)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private UUID freeStyleId;

        @NotBlank
        @Schema(
                description = "Freestyle 잡 이름 (Jenkins 내에서의 Job 식별자)",
                example = "build-and-deploy",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String jobName;

        @Schema(
                description = "Freestyle 잡 설명",
                example = "이 잡은 프로젝트를 빌드하고 배포합니다.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String description;

        @Schema(
                description = "프로젝트 관련 URL",
                example = "https://github.com/example/project",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String projectUrl;

        @Schema(
                description = "Jenkins UI에 표시될 프로젝트 이름",
                example = "Example Project",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String projectDisplayName;

        @Schema(
                description = "GitHub Webhook 트리거 활성화 여부",
                example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private Boolean githubTrigger;

        @Schema(
                description = "빌드 대상 Git 저장소 URL",
                example = "https://github.com/example/project.git",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String repositoryUrl;

        @Schema(
                description = "빌드 대상 Git 브랜치",
                example = "main",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String branch;

        @Schema(
                description = "FreeStyle shell script 내용",
                example = "#!/bin/bash\n\necho \"Build started\"\n./gradlew build\n",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String script;
    }
}
