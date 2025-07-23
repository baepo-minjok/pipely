package com.example.backend.jenkins.calendar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CalendarResponseDto {

    public interface CalendarEvent {
        String getType();
        String  getStart();
        String  getEnd();
        String getTitle();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarBuildResDto implements CalendarEvent {
        @Builder.Default
        private String type = "BUILD";
        private String title;
        private String  start;
        private String  end;
        private String status;
        private String jobName;
        private String duration;
        private int buildNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarErrorResDto implements CalendarEvent {
        @Builder.Default
        private String type = "ERROR";
        private String title;
        private String  start;
        private String  end;
        private String message;
        private String failedStage;
        private String duration;
        private int buildNumber;
    }
}
