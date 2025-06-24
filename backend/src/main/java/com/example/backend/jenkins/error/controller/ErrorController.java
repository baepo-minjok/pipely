package com.example.backend.jenkins.error.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.error.model.dto.FailedBuildResDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.DetailInfoDto;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.error.client.JenkinsRestClient;
import com.example.backend.jenkins.error.service.ErrorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/error")
@RequiredArgsConstructor
public class ErrorController {

    private final JenkinsInfoService jenkinsInfoService;
    private final ErrorService errorService;

    @GetMapping("/recent-builds/{infoId}")
    public BaseResponse<List<FailedBuildResDto>> getRecentBuilds(
            @AuthenticationPrincipal Users user,
            @PathVariable UUID infoId
    ) {
        DetailInfoDto jenkinsInfo = jenkinsInfoService.getDetailInfoById(infoId);

        JenkinsRestClient client = new JenkinsRestClient(
                jenkinsInfo.getUri(),
                jenkinsInfo.getJenkinsId(),
                jenkinsInfo.getSecretKey()
        );

        List<FailedBuildResDto> builds = errorService.getRecentBuilds(client);
        return BaseResponse.success(builds);
    }
    @GetMapping("/failed-builds/{infoId}")
    public BaseResponse<List<FailedBuildResDto>> getFailedBuilds(
            @AuthenticationPrincipal Users user,
            @PathVariable UUID infoId
    ) {
        DetailInfoDto jenkinsInfo = jenkinsInfoService.getDetailInfoById(infoId);

        JenkinsRestClient client = new JenkinsRestClient(
                jenkinsInfo.getUri(),
                jenkinsInfo.getJenkinsId(),
                jenkinsInfo.getSecretKey()
        );

        List<FailedBuildResDto> builds = errorService.getFailedBuilds(client);
        return BaseResponse.success(builds);
    }
}