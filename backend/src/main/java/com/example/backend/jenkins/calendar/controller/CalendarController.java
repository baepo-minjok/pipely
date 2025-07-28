package com.example.backend.jenkins.calendar.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarSummaryRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventGroupRes;
import com.example.backend.jenkins.calendar.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Calendar API", description = "캘린더 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(
            summary = "특정 날짜의 이벤트 조회",
            description = "infoId로 연결된 Jenkins 계정의 모든 Job 중, 해당 날짜(date)에 수행된 빌드/에러 이벤트 리스트를 반환합니다."
    )
    @GetMapping("/events/by-date")
    public ResponseEntity<BaseResponse<List<CalendarEventRes>>> getEventsByDate(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "JenkinsInfo 식별자", required = true)
            @RequestParam UUID infoId,
            @Parameter(description = "날짜 (yyyy-MM-dd)", required = true, example = "2025-07-24")
            @RequestParam String date
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                calendarService.getEventsByDate(user, infoId, date)
        ));
    }

    @Operation(
            summary = "특정 날짜의 빌드 요약",
            description = "infoId로 연결된 Jenkins 계정의 모든 Job에서, 해당 날짜(date)에 수행된 빌드/에러의 개수를 반환합니다."
    )
    @GetMapping("/summary/by-date")
    public ResponseEntity<BaseResponse<CalendarSummaryRes>> getSummaryByDate(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "JenkinsInfo 식별자", required = true)
            @RequestParam UUID infoId,
            @Parameter(description = "날짜 (yyyy-MM-dd)", required = true, example = "2025-07-24")
            @RequestParam String date
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                calendarService.getCalendarSummaryByDate(user, infoId, date)
        ));
    }

    @Operation(
            summary = "월 단위 이벤트 조회",
            description = "infoId로 연결된 Jenkins 계정의 모든 Job 중, 지정된 연/월에 수행된 모든 이벤트를 날짜별로 묶어 반환합니다."
    )
    @GetMapping("/events/by-month")
    public ResponseEntity<BaseResponse<List<CalendarEventGroupRes>>> getEventsByMonth(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam UUID infoId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                calendarService.getEventsByMonth(user, infoId, year, month)
        ));
    }

}
