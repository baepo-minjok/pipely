package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.job.model.FreeStyleHistory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class JobRequestDto {

    @Getter
    public static class CreateFreeStyleDto {

        private UUID infoId;

        private String jobName;

        private String description;
        private String projectUrl;
        private String projectDisplayName;
        private Boolean githubTrigger;
        private String repositoryUrl;
        private String branch;
        private String script;
    }

    @Getter
    public static class UpdateFreeStyleDto {

        private UUID infoId;
        private UUID freeStyleId;
        private String jobName;
        private String description;
        private String projectUrl;
        private String projectDisplayName;
        private Boolean githubTrigger;
        private String repositoryUrl;
        private String branch;
        private String script;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LightHistoryDto {

        private UUID id;
        private int version;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailHistoryDto {

        private UUID id;

        private String jobName;

        private String description;

        private String projectUrl;

        private String projectDisplayName;

        private Boolean githubTrigger;

        private String repositoryUrl;

        private String branch;

        private String script;

        private String config;

        private Integer version;

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
