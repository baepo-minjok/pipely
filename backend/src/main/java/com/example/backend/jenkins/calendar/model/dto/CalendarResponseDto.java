package com.example.backend.jenkins.calendar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CalendarResponseDto {

    public interface CalendarEvent {
        String getType();
        long getStart();
        long getEnd();
        String getTitle();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarBuildResDto implements CalendarEvent {
        private String type = "BUILD";
        private String title;
        private long start;
        private long end;
        private String status;
        private String jobName;
        private int buildNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarErrorResDto implements CalendarEvent {
        private String type = "ERROR";
        private String title;
        private long start;
        private long end;
        private String message;
        private String failedStage;
        private int buildNumber;
    }
}
