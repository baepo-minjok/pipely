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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/build")
@Tag(name = "Build", description = "Jenkins 빌드 관련 API")
public class BuildController {

    private final BuildService buildService;

    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #pipeLine)")
    @Operation(summary = "Job의 스테이지 목록 조회", description = "특정 Job에 설정된 Jenkins 스테이지 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = BuildResponseDto.Stage.class)))
    @GetMapping("/stage")
    public ResponseEntity<BaseResponse<BuildResponseDto.Stage>> getScript(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam UUID pipeLine) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getJobPipelineStage(pipeLine)));
    }

    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #dto.pipeLine)")
    @Operation(summary = "특정 스테이지 실행", description = "파라미터에 해당하는 Jenkins 스테이지만 실행합니다.")
    @PostMapping("/stage/trigger")
    public ResponseEntity<BaseResponse<String>> Steps(
            @AuthenticationPrincipal(expression = "userEntity") Users user,

            @RequestBody BuildRequestDto.BuildStageRequestDto dto) {
        buildService.StageJenkinsBuild(dto);
        return ResponseEntity.ok(BaseResponse.success("특정 Steps 실행"));
    }


    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #dto.pipeLine)")
    @Operation(summary = "빌드 이력 조회", description = "특정 Job의 빌드 이력을 조회합니다.")
    @PostMapping("/builds")
    public ResponseEntity<BaseResponse<?>> getBuildsHistory(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody BuildRequestDto.getBuildHistory dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildInfo(dto)));
    }

    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #dto.pipeLine)")
    @Operation(summary = "빌드 로그 조회", description = "특정 빌드의 로그를 조회합니다.")
    @PostMapping("/log")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildLogDto>> getBuildLog(
            @AuthenticationPrincipal(expression = "userEntity") Users user,

            @RequestBody BuildRequestDto.GetLogRequestDto dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildLog(dto)));
    }

    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #pipeLine)")
    @Operation(summary = "빌드 실시간 로그 조회", description = "빌드 로그를 실시간으로 조회합니다.")
    @GetMapping(value = "/streamlog")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildStreamLogDto>> streamLog(
            @AuthenticationPrincipal(expression = "userEntity") Users user,

            @RequestParam UUID pipeLine) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getStreamLog(pipeLine)));
    }
}
