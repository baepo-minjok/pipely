package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.job.model.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResponseDto {

    public static LightJobDto entityToLightJobDto(Job job) {
        return LightJobDto.builder()
                .jobId(job.getId())
                .name(job.getName())
                .description(job.getDescription())
                .githubUrl(job.getGithubUrl())
                .build();
    }

    public static DetailJobDto entityToDetailJobDto(Job job) {
        return DetailJobDto.builder()
                .jobId(job.getId())
                .name(job.getName())
                .description(job.getDescription())
                .githubUrl(job.getGithubUrl())
                .branch(job.getBranch())
                .trigger(job.getTrigger())
                .isBuildSelected(job.getIsBuildSelected())
                .isTestSelected(job.getIsTestSelected())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LightJobDto {

        private UUID jobId;

        private String name;

        private String description;

        private String githubUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailJobDto {

        private UUID jobId;

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

    }
}
