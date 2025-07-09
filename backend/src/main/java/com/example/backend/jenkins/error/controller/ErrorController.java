package com.example.backend.jenkins.error.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.error.model.dto.*;
import com.example.backend.jenkins.error.service.ErrorService;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.repository.FreeStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jenkins-error")
@RequiredArgsConstructor
public class ErrorController {

    private final ErrorService errorService;
    private final FreeStyleRepository freeStyleRepository;

    // 특정 Job의 최근 빌드 1건 조회 API
    @PostMapping("/recent")
    public ResponseEntity<BaseResponse<FailedBuildResDto>> getRecentBuild(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody JobReqDto request
    ) {
        FailedBuildResDto build = errorService.getRecentBuildByJob(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success(build));
    }


    // 특정 Job의 전체 빌드 내역 조회 API (성공/실패 모두 포함)
    @PostMapping("/history")
    public ResponseEntity<BaseResponse<List<FailedBuildResDto>>> getBuildsByJob(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody JobReqDto request
    ) {
        List<FailedBuildResDto> builds = errorService.getBuildsForJobByUser(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    // 특정 Job의 실패한 빌드 내역만 조회 API
    @PostMapping("/history/failed")
    public ResponseEntity<BaseResponse<List<FailedBuildResDto>>> getFailedBuildsByJob(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody JobReqDto request
    ) {
        List<FailedBuildResDto> builds = errorService.getFailedBuildsForJobByUser(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success(builds));
    }


    // 전체 Job 목록에 대한 최근 빌드 목록 조회 API
    @PostMapping("/recent/all")
    public ResponseEntity<BaseResponse<List<FailedBuildResDto>>> getAllRecentBuilds(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody JenkinsReqDto request
    ) {
        JenkinsInfo info = errorService.getJenkinsInfoByIdAndUser(request.getInfoId(), user.getId());
        List<FailedBuildResDto> builds = errorService.getRecentBuilds(info);
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    // 전체 Job 목록에 대한 실패한 빌드만 조회
    @PostMapping("/failed/all")
    public ResponseEntity<BaseResponse<List<FailedBuildResDto>>> getFailedBuilds(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody JenkinsReqDto request
    ) {
        JenkinsInfo info = errorService.getJenkinsInfoByIdAndUser(request.getInfoId(), user.getId());
        List<FailedBuildResDto> builds = errorService.getFailedBuilds(info);
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    // 특정 Job의 실패한 빌드 1건데 대해 LLM(GPT)으로 자연어 응답 제공
    @PostMapping("/summary")
    public ResponseEntity<BaseResponse<FailedBuildSummaryResDto>> getBuildSummaryWithSolution(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody JobSummaryReqDto request
    ) {
        FailedBuildSummaryResDto builds = errorService.summarizeBuildByJob(request, user.getId());
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    @PostMapping("/retry")
    public ResponseEntity<BaseResponse<String>> retryWithRollback(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody RetryReqDto request
    ) {
        errorService.retryWithRollback(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success("Retry with rollback triggered."));
    }

    @PostMapping("/retry/pipeline")
    public ResponseEntity<BaseResponse<String>> retryWithRollbackByPipeline(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody RetryReqDto request
    ) {
        errorService.retryWithRollbackByPipeline(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success("Retry with rollback triggered."));
    }

}
