package com.example.backend.jenkins.build.model.dto;

import com.example.backend.jenkins.build.model.JobType;
import lombok.*;

import java.util.List;
import java.util.Map;

public class BuildRequestDto {


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public class BuildTriggerRequestDto {

        private String jobName;
        private Map<String, Boolean> stepToggles;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class TriggerSettingRequestDto {
        private String jobName;
        private List<String> steps;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class BuildQueryRequestDto {
        private String jobName;
        private JobType jobType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class BuildLogRequestDto {


        private String jobName;
        private String buildNumber;


    }


}
