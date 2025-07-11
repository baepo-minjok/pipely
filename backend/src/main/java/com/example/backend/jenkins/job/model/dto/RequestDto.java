package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.Script;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

public class RequestDto {

    public static CreateDto toCreateDto(UpdateDto requestDto) {
        return CreateDto.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .trigger(requestDto.getTrigger())
                .schedule(requestDto.getSchedule())
                .build();
    }

    public static Pipeline toEntity(CreateDto requestDto, JenkinsInfo info, Script script, String config) {

        return Pipeline.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .isTriggered(requestDto.getTrigger())
                .schedule(requestDto.getSchedule())
                .jenkinsInfo(info)
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .script(script)
                .config(config)
                .build();
    }


    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScriptBaseDto {

        private UUID scriptId;
        private String githubUrl;
        private String branch;
        private Boolean isBuildSelected;
        private Boolean isTestSelected;
        private Boolean isK8sDeploy;
        private String tag;
        private String sshKeyPath;
        private String sshPort;
        private String deployTarget;
        private String k8sPath;
        private String deploymentName;
        private String namespace;
        private String appName;
        private String containerName;
        private String imageRepo;
        private String port;
        private String springProfile;
        private String replicas;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateDto {

        // jenkins 정보 id
        private UUID infoId;

        private UUID scriptId;

        // job 이름
        private String name;

        // job 설명
        private String description;

        // git webhook trigger 설정 여부
        private Boolean trigger;

        // 스케줄 설정
        private String schedule;
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateDto {

        // job 고유id
        private UUID pipelineId;

        private UUID scriptId;

        // job 이름
        private String name;

        // job 설명
        private String description;

        // git webhook trigger 설정 여부
        private Boolean trigger;

        // 스케줄 설정
        private String schedule;
    }
}
