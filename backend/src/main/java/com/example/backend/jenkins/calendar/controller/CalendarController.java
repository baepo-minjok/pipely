package com.example.backend.jenkins.calendar.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarSummaryRes;
import com.example.backend.jenkins.calendar.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Calendar API", description = "캘린더 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/events/by-date")
    public ResponseEntity<BaseResponse<List<CalendarEventRes>>> getEventsByDate(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam UUID infoId,
            @RequestParam String date
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                calendarService.getEventsByDate(user, infoId, date)
        ));
    }

    @Operation(summary = "특정 날짜 요약", description = "특정 날짜 기준 BUILD/ERROR 개수 반환")
    @GetMapping("/summary/by-date")
    public ResponseEntity<BaseResponse<CalendarSummaryRes>> getSummaryByDate(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam UUID infoId,
            @RequestParam String date
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                calendarService.getCalendarSummaryByDate(user, infoId, date)
        ));
    }
}
