package com.example.backend.jenkins.calendar.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarEventReq {
        @NotNull
        private UUID infoId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarSummaryByDateReq {
        @NotNull
        private UUID infoId;

        @NotBlank
        private String date; // yyyy-MM-dd
    }


}
