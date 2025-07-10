package com.example.backend.jenkins.error.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedBuildResDto {

    @Schema(description = "잡 이름", example = "my-job")
    private String jobName;

    @Schema(description = "빌드 번호", example = "42")
    private int buildNumber;

    @Schema(description = "빌드 결과 상태", example = "FAILURE")
    private String result;

    @Schema(description = "빌드 시작 시각 (yyyy-MM-dd HH:mm:ss)", example = "2025-07-09 17:23:10")
    private String timestamp;

    @Schema(description = "빌드 수행 시간", example = "1분 12초")
    private String duration;

    // timestamp -> yyyy-MM-dd HH:mm:ss
    // ms -> 초로 표기
    public static FailedBuildResDto of(String jobName, int buildNumber, String result, long timestampMillis, long durationMillis) {
        return FailedBuildResDto.builder()
                .jobName(jobName)
                .buildNumber(buildNumber)
                .result(result)
                .timestamp(formatTimestamp(timestampMillis))
                .duration(formatDuration(durationMillis))
                .build();
    }

    private static String formatTimestamp(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of("Asia/Seoul"))  // 한국 시간
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String formatDuration(long millis) {
        if (millis < 1000) {
            double seconds = millis / 1000.0;
            return String.format("%.2f초", seconds);
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes > 0) {
            return minutes + "분 " + remainingSeconds + "초";
        } else {
            return remainingSeconds + "초";
        }
    }
}
