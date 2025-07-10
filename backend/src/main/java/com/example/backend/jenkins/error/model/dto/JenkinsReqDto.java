package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "JenkinsInfo Id", example = "47a00e4f-5da6-41dd-a54d-efb6676e9485")
    private UUID infoId;

    @Schema(description = "Job 이름", example = "build-api")
    private String jobName;
}
