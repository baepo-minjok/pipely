package com.example.backend.jenkins.calendar.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class CalendarResponseDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarEventRes {

        @Schema(description = "이벤트 타입 (BUILD 또는 ERROR)", example = "BUILD")
        private String type;

        @Schema(description = "Jenkins Job 이름", example = "frontend-ui")
        private String jobName;

        @Schema(description = "빌드 번호", example = "42")
        private int buildNumber;

        @Schema(description = "빌드 시작 시간 (yyyy-MM-dd HH:mm:ss)", example = "2025-07-24 13:20:00")
        private String start;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarSummaryRes {

        @Schema(description = "해당 날짜의 성공한 빌드 수", example = "3")
        private int buildCount;

        @Schema(description = "해당 날짜의 실패한 빌드 수", example = "1")
        private int errorCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarEventGroupRes {

        @Schema(description = "날짜 (yyyy-MM-dd)", example = "2025-07-24")
        private String date;

        @Schema(description = "해당 날짜의 이벤트 목록")
        private List<CalendarEventRes> events;
    }
}
