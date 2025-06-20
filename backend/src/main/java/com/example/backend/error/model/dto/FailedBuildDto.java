package com.example.backend.error.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedBuildDto {
    private String jobName;
    private int buildNumber;
    private String result;
    private String timestamp;
    private String duration;
}
