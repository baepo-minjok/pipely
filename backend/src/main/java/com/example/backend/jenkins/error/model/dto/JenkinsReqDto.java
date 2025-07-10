package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Schema(
            description = "JenkinsInfo Id",
            example = "47a00e4f-5da6-41dd-a54d-efb6676e9485"
    )
    @NotNull(message = "infoId는 필수입니다.")
    private UUID infoId;

    @Schema(
            description = "Job 이름",
            example = "build-api"
    )
    @NotBlank(message = "jobName은 비어 있을 수 없습니다.")
    private String jobName;
}
