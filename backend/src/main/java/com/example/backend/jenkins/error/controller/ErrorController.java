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
import com.example.backend.jenkins.job.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jenkins/error")
@RequiredArgsConstructor
public class ErrorController {

    private final ErrorService errorService;

    @Operation(
            summary = "특정 Job의 실패한 빌드 조회",
            description = "선택한 Job에서 실패한 빌드 기록만 반환합니다."
    )
    @PostMapping("/failed/job")
    public ResponseEntity<BaseResponse<List<FailedBuild>>> getFailedBuildsByJob(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid JobDto request
    ) {
        List<FailedBuild> builds = errorService.getFailedBuildsForJobByUser(request.getJobId(), user.getId());
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
    @PostMapping("/failed/summary")
    public ResponseEntity<BaseResponse<FailedBuildSummary>> getBuildSummaryWithSolution(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid JobSummaryDto request
    ) {
        FailedBuildSummary builds = errorService.summarizeBuildByJob(request, user.getId());
        return ResponseEntity.ok(BaseResponse.success(builds));
    }

    @Operation(
            summary = "실패한 Job을 직전 성공한 버전으로 롤백",
            description = "최근 빌드가 실패한 Job을 가장 마지막으로 성공한 버전(PipelineVersion)으로 롤백합니다."
    )
    @PostMapping("/rollback/success")
    public ResponseEntity<BaseResponse<String>> rollbackToLastSuccessVersion(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid ErrorRequestDto.JobDto request
    ) {
        errorService.rollbackToLastSuccessfulVersion(request.getJobId(), user.getId());
        return ResponseEntity.ok(BaseResponse.success("최근 성공한 버전으로 롤백 완료"));
    }

}
