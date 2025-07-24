package com.example.backend.jenkins.calendar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CalendarResponseDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarEventRes {
        private String type;        // BUILD or ERROR
        private String jobName;
        private int buildNumber;
        private String start;       // yyyy-MM-dd HH:mm:ss
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarSummaryRes {
        private int buildCount;
        private int errorCount;
    }


}
