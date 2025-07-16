package com.example.backend.jenkins.calendar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class CalendarRequestDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarBuildDto {
        private UUID pipeLine;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarErrorDto {
        private UUID pipeLine;
    }
}
