package com.example.backend.jenkins.build.model.dto;


import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BuildTriggerRequestDto {

    private String jobName;
    private Map<String, Boolean> stepToggles;
}
