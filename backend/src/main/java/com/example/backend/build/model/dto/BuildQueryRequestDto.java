package com.example.backend.build.model.dto;

import com.example.backend.build.model.JobType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class BuildQueryRequestDto {
    private String jobName;
    private JobType jobType;






}
