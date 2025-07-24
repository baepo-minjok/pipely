package com.example.backend.jenkins.calendar.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.calendar.model.dto.CalendarRequestDto.CalendarErrorDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarRequestDto.CalendarBuildDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarBuildResDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarErrorResDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarSummaryRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarRequestDto.CalendarEventReq;
import com.example.backend.jenkins.calendar.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Calendar API", description = "캘린더 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "빌드 캘린더 이벤트 조회", description = "특정 파이프라인의 Jenkins 빌드 이력을 캘린더 형태로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 누락 또는 파싱 실패"),
            @ApiResponse(responseCode = "500", description = "Jenkins 서버 오류")
    })
    @PostMapping("/build")
    public ResponseEntity<BaseResponse<List<CalendarBuildResDto>>> getBuildCalendar(
            @RequestBody @Valid CalendarBuildDto dto
    ) {
        return ResponseEntity.ok(BaseResponse.success(calendarService.getBuildCalendar(dto.getPipeLine())));
    }

    @Operation(summary = "에러 캘린더 이벤트 조회", description = "특정 파이프라인의 빌드 실패 이력을 캘린더 형태로 조회합니다.")
    @PostMapping("/error")
    public ResponseEntity<BaseResponse<List<CalendarErrorResDto>>> getErrorCalendar(
            @RequestBody @Valid CalendarErrorDto dto
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                calendarService.getErrorCalendar(dto.getPipeLine())
        ));
    }

    @Operation(summary = "전체 Job 이벤트 리스트", description = "infoId로 연결된 Jenkins 계정 전체 Job의 빌드/에러 이벤트 리스트 조회")
    @PostMapping("/events")
    public ResponseEntity<BaseResponse<List<CalendarEventRes>>> getAllCalendarEvents(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid CalendarEventReq req
    ) {
        return ResponseEntity.ok(BaseResponse.success(calendarService.getCalendarEventList(user, req.getInfoId())));
    }

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

    @Operation(summary = "캘린더 빌드/에러 요약", description = "날짜별 buildCount / errorCount 반환")
    @PostMapping("/summary")
    public ResponseEntity<BaseResponse<Map<String, CalendarSummaryRes>>> getSummary(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid CalendarEventReq req
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                calendarService.getCalendarSummary(user, req.getInfoId())
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
