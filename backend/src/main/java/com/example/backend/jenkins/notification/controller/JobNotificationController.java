package com.example.backend.jenkins.notification.controller;

import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.notification.model.dto.RequestDto;
import com.example.backend.jenkins.notification.model.dto.ResponseDto;
import com.example.backend.jenkins.notification.service.JobNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/jobNotification")
@Tag(name = "Job Notification 관리", description = "Jenkins Job 알림 설정 API")
public class JobNotificationController {

    private final JobNotificationService jobNotificationService;

    @Operation(summary = "Job 알림 정보 생성", description = "여러 개의 Jenkins Job 알림 정보를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(
            @RequestBody(description = "생성할 알림 정보 목록", required = true)
            List<RequestDto.createCredential> dtoList,
            @AuthenticationPrincipal CustomUserDetails user) {

        jobNotificationService.createJobNotifications(dtoList, user.getUser().getId());
        return ResponseEntity.ok(
                BaseResponse.success("create jenkins notification credential success")
        );
    }

    @Operation(summary = "Job 알림 목록 조회", description = "특정 Job ID에 대한 사용자 알림 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<ResponseDto.JobNotificationListResponseDto>>> getNotificationsForUser(
            @RequestBody(description = "Job ID 포함 요청 객체", required = true)
            RequestDto.NotificationListRequestDto request
    ) {
        UUID userId = getCurrentUserId();
        List<ResponseDto.JobNotificationListResponseDto> notifications =
                jobNotificationService.getUserJobNotifications(userId, request.getJobId());

        return ResponseEntity.ok()
                .body(BaseResponse.success(notifications));
    }

    @Operation(summary = "Job 알림 상세 조회", description = "알림 이름으로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "알림 자격 정보 없음")
    })
    @PostMapping("/detail")
    public ResponseEntity<BaseResponse<ResponseDto.JobNotificationDetailResponseDto>> getNotificationDetail(
            @RequestBody(description = "알림 이름 요청 객체", required = true)
            RequestDto.NotificationDetailRequestDto requestDto
    ) {
        ResponseDto.JobNotificationDetailResponseDto dto =
                jobNotificationService.getNotificationDetail(requestDto.getCredentialName());
        return ResponseEntity.ok()
                .body(BaseResponse.success(dto));
    }

    @Operation(summary = "Job 알림 정보 수정", description = "알림 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/update")
    public ResponseEntity<BaseResponse<String>> updateNotification(
            @RequestBody(description = "수정할 알림 정보", required = true)
            RequestDto.JobNotificationUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getUser().getId();
        jobNotificationService.updateNotification(dto, userId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("update jenkins notification credential success"));
    }

    @Operation(summary = "Job 알림 정보 삭제", description = "알림 정보를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "요청 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteJobNotification(
            @RequestBody(description = "삭제할 알림 정보", required = true)
            RequestDto.JobNotificationDeleteRequestDto dto) {

        UUID userId = getCurrentUserId();
        jobNotificationService.deleteNotification(dto, userId);

        return ResponseEntity.ok()
                .body(BaseResponse.success("delete jenkins notification credential success"));
    }

    private UUID getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getUser().getId();
    }
}