package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.JobNotification;
import com.example.backend.jenkins.job.model.dto.JobNotificationRequestDto;
import com.example.backend.jenkins.job.service.JobNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/jobNotification")
public class JobNotificationController {
    private final JobNotificationService jobNotificationService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(@RequestBody JobNotificationRequestDto.createCredential dto,
                                                  @AuthenticationPrincipal CustomUserDetails user) {
        jobNotificationService.createJobNotification(dto, user.getUser().getId());
        return ResponseEntity.ok()
                .body(BaseResponse.success("create jenkins notification credential success"));
    }

    @PostMapping("/createNotifyScript")
    public ResponseEntity<BaseResponse<String>> sendNotification(@RequestBody JobNotificationRequestDto.SendJobNotificationRequestDto dto) {
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
