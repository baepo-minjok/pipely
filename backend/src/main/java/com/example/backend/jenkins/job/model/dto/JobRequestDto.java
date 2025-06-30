package com.example.backend.jenkins.job.model.dto;

import lombok.Getter;

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
}
