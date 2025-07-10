package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FailedBuildSummaryResDto {

    @Schema(description = "잡 이름", example = "deploy-service")
    private String jobName;

    @Schema(description = "실패한 빌드 번호", example = "102")
    private int buildNumber;

    @Schema(description = "GPT 기반 자연어 요약 및 해결 방안", example = "빌드는 테스트 실패로 인해 종료되었습니다. 테스트 케이스를 검토하세요.")
    private String naturalResponse;
}
