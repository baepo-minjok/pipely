package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.Script;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResponseDto {

    public static LightJobDto entityToLightJobDto(Pipeline pipeline) {
        return LightJobDto.builder()
                .pipelineId(pipeline.getId())
                .name(pipeline.getName())
                .description(pipeline.getDescription())
                .build();
    }

    public static DetailJobDto entityToDetailJobDto(Pipeline pipeline) {
        return DetailJobDto.builder()
                .pipelineId(pipeline.getId())
                .name(pipeline.getName())
                .description(pipeline.getDescription())
                .trigger(pipeline.getIsTriggered())
                .createdAt(pipeline.getCreatedAt())
                .updatedAt(pipeline.getUpdatedAt())
                .deletedAt(pipeline.getDeletedAt())
                .build();
    }

    public static LightScriptDto entityToLightScriptDto(Script script) {
        return LightScriptDto.builder()
                .scriptId(script.getId())
                .script(script.getScript())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LightJobDto {

        private UUID pipelineId;

        private String name;

        private String description;

        private String githubUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailJobDto {

        private UUID pipelineId;

        // job 이름
        private String name;

        // job 설명
        private String description;

        // 연동된 git 주소
        private String githubUrl;

        // clone할 branch 이름
        private String branch;

        // git webhook trigger 설정 여부
        private Boolean trigger;

        // Build Stage가 선택되었는지 여부
        private Boolean isBuildSelected;

        // Test Stage가 선택되었는지 여부
        private Boolean isTestSelected;

        // 생성 시간
        private LocalDateTime createdAt;

        // 수정 시간
        private LocalDateTime updatedAt;

        private LocalDateTime deletedAt;

        private String script;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LightScriptDto {

        private UUID scriptId;

        private String script;
    }
}
