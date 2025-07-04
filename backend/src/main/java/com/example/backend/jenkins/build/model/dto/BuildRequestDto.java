package com.example.backend.jenkins.build.model.dto;

import com.example.backend.jenkins.build.model.JobType;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BuildRequestDto {


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class BuildStageRequestDto {

        private String jobName;
        private Map<String, Boolean> stageToggles;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageSettingRequestDto {
        private String jobName;
        private List<String> stage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildQueryRequestDto {
        private String jobName;
        private JobType jobType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildLogRequestDto {


        private String jobName;
        private String buildNumber;


    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetScheduleJob {

        private String jobName;
        private String cron;
        private UUID JobStyleId;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetLogRequestDto {

        private String jobName;
        private String buildNumber;
        private UUID JobStyleId;

    }



}
