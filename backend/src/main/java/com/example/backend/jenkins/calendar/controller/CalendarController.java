package com.example.backend.jenkins.calendar.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto;
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
            summary = "월 단위 이벤트 조회",
            description = "infoId로 연결된 Jenkins 계정의 모든 Job 중, 지정된 연/월에 수행된 모든 이벤트를 날짜별로 묶어 반환합니다."
    )
    @GetMapping("/events/by-month")
    public ResponseEntity<BaseResponse<List<CalendarEventGroupRes>>> getEventsByMonth(
            @Parameter(description = "로그인한 사용자 정보", hidden = true)
            @AuthenticationPrincipal(expression = "userEntity") Users user,

            @Parameter(description = "Jenkins 서버 정보 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID infoId,

            @Parameter(description = "조회할 연도", required = true, example = "2025")
            @RequestParam int year,

            @Parameter(description = "조회할 월 (1~12)", required = true, example = "7")
            @RequestParam int month
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                calendarService.getEventsByMonth(user, infoId, year, month)
        ));
    }
}
