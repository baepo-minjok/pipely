package com.example.backend.jenkins.notification.controller;

import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.notification.model.dto.RequestDto;
import com.example.backend.jenkins.notification.model.dto.ResponseDto;
import com.example.backend.jenkins.notification.service.JobNotificationService;
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
public class JobNotificationController {
    private final JobNotificationService jobNotificationService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(@RequestBody RequestDto.createCredential dto,
                                                  @AuthenticationPrincipal CustomUserDetails user) {
        jobNotificationService.createJobNotification(dto, user.getUser().getId());
        return ResponseEntity.ok()
                .body(BaseResponse.success("create jenkins notification credential success"));
    }

    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<ResponseDto.JobNotificationListResponseDto>>> getNotificationsForUser(
            @RequestBody RequestDto.NotificationListRequestDto request
    ) {
        UUID userId = getCurrentUserId();
        List<ResponseDto.JobNotificationListResponseDto> notifications =
                jobNotificationService.getUserJobNotifications(userId, request.getJobId());

        return ResponseEntity.ok()
                .body(BaseResponse.success(notifications));
    }

    @PostMapping("/detail")
    public ResponseEntity<BaseResponse<ResponseDto.JobNotificationDetailResponseDto>> getNotificationDetail(
            @RequestBody RequestDto.NotificationDetailRequestDto requestDto
    ) {
        ResponseDto.JobNotificationDetailResponseDto dto =
                jobNotificationService.getNotificationDetail(requestDto.getCredentialName());
        return ResponseEntity.ok()
                .body(BaseResponse.success(dto));
    }

    @PostMapping("/update")
    public ResponseEntity<BaseResponse<String>> updateNotification(
            @RequestBody RequestDto.JobNotificationUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getUser().getId();
        jobNotificationService.updateNotification(dto, userId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("update jenkins notification credential success"));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteJobNotification(
            @RequestBody RequestDto.JobNotificationDeleteRequestDto dto) {

        UUID userId = getCurrentUserId();
        jobNotificationService.deleteNotification(dto, userId);

        return ResponseEntity.ok()
                .body(BaseResponse.success("delete jenkins notification credential success"));
    }

    @PostMapping("/createNotifyScript")
    public ResponseEntity<BaseResponse<String>> sendNotification(@RequestBody RequestDto.SendJobNotificationRequestDto dto) {
        UUID currentUserId = getCurrentUserId();
        jobNotificationService.createNotifyScript(currentUserId, dto.getJobId());
        return ResponseEntity.ok()
                .body(BaseResponse.success("create jenkins notification script success"));
    }

    private UUID getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getUser().getId();
    }
}
