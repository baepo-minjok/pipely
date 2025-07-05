package com.example.backend.jenkins.job.model.dto.pipeline;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.UUID;

public class PipelineScriptRequestDto {

    @Getter
    @Schema(name = "CreatePipelineScriptDto", description = "스크립트 입력 방식 Pipeline Job 생성 요청")
    public static class CreatePipelineScriptDto {

        @Schema(
                description = "JenkinsInfo 고유 식별자 (UUID)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        private UUID infoId;

        @NotBlank
        @Schema(
                description = "파이프라인 잡 이름",
                example = "pipeline-script-job"
        )
        private String jobName;

        @Schema(description = "잡 설명", example = "이 잡은 pipeline DSL script로 실행됩니다.")
        private String description;

        @Schema(description = "프로젝트 관련 URL", example = "https://github.com/example/project")
        private String projectUrl;

        @Schema(description = "프로젝트 표시 이름", example = "Example Pipeline Project")
        private String projectDisplayName;

        @Schema(description = "GitHub Webhook 트리거 활성화 여부", example = "true")
        private Boolean githubTrigger;

        @Schema(description = "Jenkins Pipeline DSL script", example = "pipeline { agent any ... }")
        private String script;

//        @Schema(description = "Jenkins 파라미터 목록")
//        private List<ParameterDto> parameters; //  파라미터 여러개 받을 수 있음
    }

//    @Getter
//    @Setter
//    @Schema(description = "Jenkins Job에서 사용하는 개별 파라미터")
//    public class ParameterDto {
//
//        @NotBlank
//        private String name;
//
//        private String description;
//
//        private String defaultValue;
//
//        private Boolean trim;
//    }


}
