package com.example.backend.jenkins.build.model.dto;

import com.example.backend.jenkins.build.model.JobType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class BuildQueryRequestDto {
    private String jobName;
    private JobType jobType;
}
