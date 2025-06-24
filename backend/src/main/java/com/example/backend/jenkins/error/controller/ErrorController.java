package com.example.backend.jenkins.error.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.error.model.dto.FailedBuildResDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.DetailInfoDto;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.error.client.JenkinsRestClient;
import com.example.backend.jenkins.error.service.ErrorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jenkins-error")
@RequiredArgsConstructor
public class ErrorController {

    private final JenkinsInfoService jenkinsInfoService;
    private final ErrorService errorService;

    // 특정 Job의 최근 빌드 1건 조회 API
    @GetMapping("/recent")
    public ResponseEntity<BaseResponse<FailedBuildResDto>> getRecentBuild(
            @AuthenticationPrincipal Users user,
            @RequestParam UUID infoId,
            @RequestParam String jobName
    ) {
        DetailInfoDto jenkinsInfo = jenkinsInfoService.getDetailInfoById(infoId);
        JenkinsRestClient client = new JenkinsRestClient(
                jenkinsInfo.getUri(),
                jenkinsInfo.getJenkinsId(),
                jenkinsInfo.getSecretKey()
        );
        FailedBuildResDto build = errorService.getRecentBuild(client, jobName);
        return ResponseEntity.ok(BaseResponse.success(build));
    }

    // 특정 Job의 전체 빌드 내역 조회 API (성공/실패 모두 포함)
    @GetMapping("/history")
    public ResponseEntity<BaseResponse<List<FailedBuildResDto>>> getBuildsByJob(
            @AuthenticationPrincipal Users user,
            @RequestParam UUID infoId,
            @RequestParam String jobName
    ) {
        DetailInfoDto jenkinsInfo = jenkinsInfoService.getDetailInfoById(infoId);
        JenkinsRestClient client = new JenkinsRestClient(
                jenkinsInfo.getUri(),
                jenkinsInfo.getJenkinsId(),
                jenkinsInfo.getSecretKey()
        );
        List<FailedBuildResDto> builds = errorService.getBuildsForJob(client, jobName);
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    // 특정 Job의 실패한 빌드 내역만 조회 API
    @GetMapping("/history/failed")
    public ResponseEntity<BaseResponse<List<FailedBuildResDto>>> getFailedBuildsByJob(
            @AuthenticationPrincipal Users user,
            @RequestParam UUID infoId,
            @RequestParam String jobName
    ) {
        DetailInfoDto jenkinsInfo = jenkinsInfoService.getDetailInfoById(infoId);
        JenkinsRestClient client = new JenkinsRestClient(
                jenkinsInfo.getUri(),
                jenkinsInfo.getJenkinsId(),
                jenkinsInfo.getSecretKey()
        );
        List<FailedBuildResDto> builds = errorService.getFailedBuildsForJob(client, jobName);
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    // 전체 Job 목록에 대한 최근 빌드 목록 조회 API
    @GetMapping("/recent/all")
    public ResponseEntity<BaseResponse<List<FailedBuildResDto>>> getAllRecentBuilds(
            @AuthenticationPrincipal Users user,
            @RequestParam UUID infoId
    ) {
        DetailInfoDto jenkinsInfo = jenkinsInfoService.getDetailInfoById(infoId);
        JenkinsRestClient client = new JenkinsRestClient(
                jenkinsInfo.getUri(),
                jenkinsInfo.getJenkinsId(),
                jenkinsInfo.getSecretKey()
        );
        List<FailedBuildResDto> builds = errorService.getRecentBuilds(client);
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    // 전체 Job 목록에 대한 실패한 빌드만 조회
    @GetMapping("/failed/all")
    public ResponseEntity<BaseResponse<List<FailedBuildResDto>>> getFailedBuilds(
            @AuthenticationPrincipal Users user,
            @RequestParam UUID infoId
    ) {
        DetailInfoDto jenkinsInfo = jenkinsInfoService.getDetailInfoById(infoId);
        JenkinsRestClient client = new JenkinsRestClient(
                jenkinsInfo.getUri(),
                jenkinsInfo.getJenkinsId(),
                jenkinsInfo.getSecretKey()
        );
        List<FailedBuildResDto> builds = errorService.getFailedBuilds(client);
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

}
