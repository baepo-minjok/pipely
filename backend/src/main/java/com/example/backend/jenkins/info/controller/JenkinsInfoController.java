package com.example.backend.jenkins.info.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.CreateDto;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jenkins/info")
@RequiredArgsConstructor
public class JenkinsInfoController {

    private final JenkinsInfoService jenkinsInfoService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<JenkinsInfo>> create(
            @AuthenticationPrincipal Users currentUser,
            @RequestBody CreateDto request) {
        JenkinsInfo created = jenkinsInfoService.createJenkinsInfo(
                currentUser.getId(),
                request.getJenkinsId(),
                request.getSecretKey(),
                request.getUri()
        );
        return ResponseEntity.ok()
                .body(BaseResponse.success(created));
    }


}
