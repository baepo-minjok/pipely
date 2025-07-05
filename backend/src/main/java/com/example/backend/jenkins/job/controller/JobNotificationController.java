package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.JobNotification;
import com.example.backend.jenkins.job.model.dto.JobNotificationRequestDto;
import com.example.backend.jenkins.job.service.JobNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/jobNotification")
public class JobNotificationController {
    private final JobNotificationService jobNotificationService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(@RequestBody JobNotificationRequestDto dto,
                                                  @AuthenticationPrincipal CustomUserDetails user) {
        jobNotificationService.createJobNotification(dto, user.getUser().getId());
        return ResponseEntity.ok()
                .body(BaseResponse.success("create jenkins notification credential success"));
    }
}
