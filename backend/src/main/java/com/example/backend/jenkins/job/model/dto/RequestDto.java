package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class RequestDto {

    public static CreateDto toCreateDto(UpdateDto requestDto) {
        return CreateDto.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .githubUrl(requestDto.getGithubUrl())
                .branch(requestDto.getBranch())
                .trigger(requestDto.getTrigger())
                .isBuildSelected(requestDto.getIsBuildSelected())
                .isTestSelected(requestDto.getIsTestSelected())
                .build();
    }

    public static Job toEntity(CreateDto requestDto, JenkinsInfo info) {
        return Job.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .githubUrl(requestDto.getGithubUrl())
                .branch(requestDto.getBranch())
                .trigger(requestDto.getTrigger())
                .isBuildSelected(requestDto.getIsBuildSelected())
                .isTestSelected(requestDto.getIsTestSelected())
                .jenkinsInfo(info)
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateDto {

        // jenkins 정보 id
        private UUID infoId;

        // job 이름
        private String name;

        // job 설명
        private String description;

        // 연동된 git 주소
        private String githubUrl;

        // git webhook trigger 설정 여부
        private Boolean trigger;

        private String script;

        // clone할 branch 이름
        private String branch;

        // Build Stage가 선택되었는지 여부
        private Boolean isBuildSelected;

        // Test Stage가 선택되었는지 여부
        private Boolean isTestSelected;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerateScriptDto {

        // 연동된 git 주소
        private String githubUrl;

        // clone할 branch 이름
        private String branch;

        // Build Stage가 선택되었는지 여부
        private Boolean isBuildSelected;

        // Test Stage가 선택되었는지 여부
        private Boolean isTestSelected;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateDto {

        // job 고유id
        private UUID jobId;

        // job 이름
        private String name;

        // job 설명
        private String description;

        // 연동된 git 주소
        private String githubUrl;

        // git webhook trigger 설정 여부
        private Boolean trigger;

        private String script;

        // clone할 branch 이름
        private String branch;

        // Build Stage가 선택되었는지 여부
        private Boolean isBuildSelected;

        // Test Stage가 선택되었는지 여부
        private Boolean isTestSelected;


    }
}
