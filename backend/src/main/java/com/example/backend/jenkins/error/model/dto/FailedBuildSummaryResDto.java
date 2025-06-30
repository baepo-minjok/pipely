package com.example.backend.jenkins.error.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FailedBuildSummaryResDto {
    private String jobName;
    private int buildNumber;
    private String naturalResponse;  // 자연어 요약 + 해결책 문장
}
