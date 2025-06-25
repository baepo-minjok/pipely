package com.example.backend.jenkins.error.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JenkinsReqDto {
    private UUID infoId;
    private String jobName;
}
