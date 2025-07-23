package com.example.backend.jenkins.calendar.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.calendar.model.dto.CalendarRequestDto.CalendarBuildDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarBuildResDto;
import com.example.backend.jenkins.calendar.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
