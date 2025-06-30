package com.example.backend.jenkins.build.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class BuildLogRequestDto {




    private String jobName;
    private String buildNumber;







}
