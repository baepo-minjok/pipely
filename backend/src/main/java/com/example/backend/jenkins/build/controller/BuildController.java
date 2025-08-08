package com.example.backend.jenkins.build.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.build.service.BuildService;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/build")
@Tag(name = "Jenkins Job의 Build 관리", description = "Jenkins 빌드를 담당하는 API입니다. Job 실행, 빌드 이력, 로그 조회 기능을 제공합니다.")
public class BuildController {

    private final BuildService buildService;
    private final PipelineService pipelineService;

    @PreAuthorize("@pipelineService.isOwner(#user, #dto.jobId)")
    @Operation(
            summary = "Job 빌드",
            description = "파라미터로 받은 스테이지는 실행에서 제외합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실행 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 스테이지를 찾을 수 없음")
    })
    @PostMapping("/stage/trigger")
    public ResponseEntity<BaseResponse<Integer>> Steps(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid BuildRequestDto.BuildStageRequestDto dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.triggerStages(dto)));
    }

    @Operation(
            summary = "빌드 이력 조회",
            description = "특정 Job의 최신 빌드 이력을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "Job(파이프라인) 없음")
    })
    @GetMapping("/history/latest")
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildInfo>> getLatestHistory(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam @NotNull UUID jobId) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getLastBuildStatus(jobId)));
    }

    @Operation(
            summary = "빌드 이력 조회",
            description = "특정 Job의 빌드 이력 전체 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "Job(파이프라인) 없음")
    })
    @GetMapping("/history/all")
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    public ResponseEntity<BaseResponse<List<BuildResponseDto.BuildInfo>>> getAllHistory(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam @NotNull UUID jobId) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildHistory(jobId)));
    }

    @PreAuthorize("@pipelineService.isOwner(#user, #dto.jobId)")
    @Operation(
            summary = "빌드 로그 조회",
            description = "특정 빌드의 전체 로그를 한 번에 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = BuildResponseDto.BuildLogDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "로그 없음")
    })
    @PostMapping("/log")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildLogDto>> getBuildLog(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid BuildRequestDto.GetLogRequestDto dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildLog(dto)));
    }

    @Operation(
            summary = "Job 빌드상태 변경",
            description = "StatusDto에 따라 빌드 상태를 BUILD_SUCCESS, BUILD_FAILURE로 변경합니다."
    )
    @PostMapping("/status")
    public ResponseEntity<BaseResponse<Boolean>> setStatus(
            @RequestBody @Valid RequestDto.StatusDto dto
    ) {
        pipelineService.setState(dto);
        return ResponseEntity.ok()
                .body(BaseResponse.success(true));
    }

    @Operation(
            summary = "최신 빌드번호 조회",
            description = "해당 job의 최신 빌드번호를 조회합니다."
    )
    @GetMapping("/buildNumber")
    public ResponseEntity<BaseResponse<Integer>> getBuildNumber(
            @RequestParam UUID jobId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(buildService.getCurrentBuildNumber(jobId)));
    }

    @Operation(
            summary = "빌드 중단",
            description = "해당 job이 빌드중이라면 빌드를 중단합니다."
    )
    @PostMapping("/stop")
    public ResponseEntity<BaseResponse<Boolean>> stop(
            @RequestParam UUID jobId
    ) {
        buildService.stopBuild(jobId);
        return ResponseEntity.ok()
                .body(BaseResponse.success(true));
    }

    @Operation(
            summary = "현재 빌드상황 조회",
            description = "해당 job이 현재 빌드중이라면 빌드 현황(로그, 남은시간, 빌드상태)를 조회합니다."
    )
    @GetMapping("/sendLog")
    public ResponseEntity<Void> test(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam UUID jobId,
            @RequestParam Integer buildNumber
    ) {
        buildService.sendLog(jobId, buildNumber, user.getEmail());
        return ResponseEntity.ok().build();
    }
}
