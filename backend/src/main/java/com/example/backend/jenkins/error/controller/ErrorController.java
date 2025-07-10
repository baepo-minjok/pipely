package com.example.backend.jenkins.error.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.error.model.dto.*;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.JobSummaryDto;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.JobDto;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.RetryDto;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto.JenkinsInfoDto;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto.FailedBuild;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto.FailedBuildSummary;
import com.example.backend.jenkins.error.service.ErrorService;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.repository.FreeStyleRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

    @Operation(
            summary = "특정 Job의 최근 빌드 조회",
            description = "선택한 Job의 가장 최근 빌드 정보를 반환합니다."
    )
    @PostMapping("/recent")
    public ResponseEntity<BaseResponse<FailedBuild>> getRecentBuild(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid JobDto request
    ) {
        FailedBuild build = errorService.getRecentBuildByJob(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success(build));
    }


    @Operation(
            summary = "특정 Job의 전체 빌드 조회",
            description = "선택한 Job의 전체 빌드 기록(성공/실패 포함)을 반환합니다."
    )
    @PostMapping("/history")
    public ResponseEntity<BaseResponse<List<FailedBuild>>> getBuildsByJob(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid JobDto request
    ) {
        List<FailedBuild> builds = errorService.getBuildsForJobByUser(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    @Operation(
            summary = "특정 Job의 실패한 빌드 조회",
            description = "선택한 Job에서 실패한 빌드 기록만 반환합니다."
    )
    @PostMapping("/history/failed")
    public ResponseEntity<BaseResponse<List<FailedBuild>>> getFailedBuildsByJob(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid JobDto request
    ) {
        List<FailedBuild> builds = errorService.getFailedBuildsForJobByUser(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success(builds));
    }


    @Operation(
            summary = "전체 Job의 최근 빌드 조회",
            description = "Jenkins 서버 내 전체 Job의 가장 최근 빌드 정보를 반환합니다."
    )
    @PostMapping("/recent/all")
    public ResponseEntity<BaseResponse<List<FailedBuild>>> getAllRecentBuilds(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid JenkinsInfoDto request
    ) {
        JenkinsInfo info = errorService.getJenkinsInfoByIdAndUser(request.getInfoId(), user.getId());
        List<FailedBuild> builds = errorService.getRecentBuilds(info);
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    @Operation(
            summary = "전체 Job의 실패한 빌드 조회",
            description = "Jenkins 서버 내 전체 Job 중 실패한 빌드 기록만 반환합니다."
    )
    @PostMapping("/failed/all")
    public ResponseEntity<BaseResponse<List<FailedBuild>>> getFailedBuilds(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid JenkinsInfoDto request
    ) {
        JenkinsInfo info = errorService.getJenkinsInfoByIdAndUser(request.getInfoId(), user.getId());
        List<FailedBuild> builds = errorService.getFailedBuilds(info);
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    @Operation(
            summary = "실패 빌드에 대한 요약 제공",
            description = "특정 Job의 실패한 빌드에 대해 LLM(GPT)을 통해 자연어 요약 및 해결 방안을 제공합니다."
    )
    @PostMapping("/summary")
    public ResponseEntity<BaseResponse<FailedBuildSummary>> getBuildSummaryWithSolution(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid JobSummaryDto request
    ) {
        FailedBuildSummary builds = errorService.summarizeBuildByJob(request, user.getId());
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    @Operation(
            summary = "빌드 실패 롤백 재시도 (파이프라인)",
            description = "Pipeline Job에서 가장 최근 실패 빌드를 마지막 성공 버전으로 롤백 후 재시도합니다."
    )
    @PostMapping("/retry")
    public ResponseEntity<BaseResponse<String>> retryWithRollback(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid RetryDto request
    ) {
        errorService.retryWithRollback(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success("Retry with rollback triggered."));
    }

}
