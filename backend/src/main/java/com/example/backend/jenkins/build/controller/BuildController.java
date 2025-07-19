package com.example.backend.jenkins.build.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.build.model.dto.*;
import com.example.backend.jenkins.build.service.BuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/build")
@Tag(name = "Build", description = "Jenkins 빌드/배포 관련 API입니다. 빌드 이력, 로그, 스테이지 등 조회 및 실행 기능 제공")
public class BuildController {

    private final BuildService buildService;

    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @Operation(
            summary = "Job의 스테이지 목록 조회",
            description = "특정 Job(파이프라인)에 등록된 Jenkins 스테이지(예: BUILD, TEST, DEPLOY 등)의 목록을 조회합니다."

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = BuildResponseDto.Stage.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "파이프라인을 찾을 수 없음")
    })
    @GetMapping("/stage")
    public ResponseEntity<BaseResponse<BuildResponseDto.Stage>> getScript(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam UUID jobId) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getJobPipelineStage(jobId)));
    }

    @PreAuthorize("@pipelineService.isOwner(#user, #dto.jobId)")
    @Operation(
            summary = "특정 스테이지 실행 (Stage Trigger)",
            description = "파라미터로 받은 스테이지만 Jenkins에서 즉시 실행합니다. 예: 'TEST' 단계만 개별 실행"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실행 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 스테이지를 찾을 수 없음")
    })
    @PostMapping("/stage/trigger")
    public ResponseEntity<BaseResponse<String>> Steps(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid BuildRequestDto.BuildStageRequestDto dto) {
        buildService.StageJenkinsBuild(dto);
        return ResponseEntity.ok(BaseResponse.success("특정 Steps 실행"));
    }

    @PreAuthorize("@pipelineService.isOwner(#user, #dto.jobId)")
    @Operation(
            summary = "빌드 이력 조회",
            description = "특정 파이프라인(Job)의 빌드 이력 전체 혹은 조건별(LATEST 등) 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "Job(파이프라인) 없음")
    })
    @PostMapping("/builds")
    public ResponseEntity<BaseResponse<?>> getBuildsHistory(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody BuildRequestDto.getBuildHistory dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildInfo(dto)));
    }

    @PreAuthorize("@pipelineService.isOwner(#user, #dto.jobId)")
    @Operation(
            summary = "빌드 로그 조회",
            description = "특정 빌드(빌드번호, 파이프라인 ID 등)의 전체 로그를 한 번에 조회합니다."
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

    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @Operation(
            summary = "빌드 실시간 로그 스트림 조회",
            description = "Jenkins에서 빌드 진행 상황을 실시간으로 스트리밍 방식으로 받아옵니다. (예: 콘솔 로그 라인별 실시간 응답)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
            , @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "실시간 로그를 찾을 수 없음")
    })
    @GetMapping(value = "/streamlog")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildStreamLogDto>> streamLog(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam UUID jobId) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getStreamLog(jobId)));
    }
}
