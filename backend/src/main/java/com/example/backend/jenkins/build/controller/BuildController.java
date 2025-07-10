package com.example.backend.jenkins.build.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/build")
@Tag(name = "Build", description = "Jenkins 빌드 관련 API")
public class BuildController {

    private final BuildService buildService;

    @Operation(summary = "Job의 스테이지 목록 조회", description = "특정 Job에 설정된 Jenkins 스테이지 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = BuildResponseDto.Stage.class)))
    @PostMapping("/stage")
    public ResponseEntity<BaseResponse<BuildResponseDto.Stage>> getScript(
            @RequestBody BuildRequestDto.GetJobNameJobRequestDto dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getJobPipelineStage(dto)));
    }

    @Operation(summary = "특정 스테이지 실행", description = "파라미터에 해당하는 Jenkins 스테이지만 실행합니다.")
    @PostMapping("/stage/trigger")
    public ResponseEntity<BaseResponse<String>> Steps(
            @RequestBody BuildRequestDto.BuildStageRequestDto dto) {
        buildService.StageJenkinsBuild(dto);
        return ResponseEntity.ok(BaseResponse.success("특정 Steps 실행"));
    }

    @Operation(summary = "스테이지 설정", description = "Jenkins Job의 스테이지 정보를 config.xml에 설정합니다.")
    @PostMapping("/stagesetting")
    public ResponseEntity<BaseResponse<String>> triggerSetting(
            @RequestBody BuildRequestDto.StageSettingRequestDto dto) {
        buildService.stagePipeline1(dto);
        return ResponseEntity.ok(BaseResponse.success("Steps 설정 완료"));
    }

    @Operation(summary = "Job 스케줄 조회", description = "Job 이름과 pipeline ID로 스케줄 정보를 조회합니다.")
    @GetMapping("/schedule")
    public ResponseEntity<BaseResponse<String>> scheduleJob(
            @Parameter(description = "Job 이름") @RequestParam String jobName,
            @Parameter(description = "Pipeline UUID") @RequestParam UUID pipelineId) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getSchedule(jobName, pipelineId)));
    }

    @Operation(summary = "Job 스케줄 설정", description = "CRON 표현식을 사용하여 Jenkins Job의 스케줄을 설정합니다.")
    @PostMapping("/setSchedule")
    public ResponseEntity<BaseResponse<String>> getscheduleJob(
            @RequestBody BuildRequestDto.SetScheduleJob req) {
        buildService.setSchedule(req);
        return ResponseEntity.ok(BaseResponse.success("스케줄 설정이 완료되었습니다."));
    }

    @Operation(summary = "빌드 이력 조회", description = "특정 Job의 빌드 이력을 조회합니다.")
    @PostMapping("/builds")
    public ResponseEntity<BaseResponse<?>> getBuildsHistory(
            @RequestBody BuildRequestDto.getBuildHistory dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildInfo(dto)));
    }

    @Operation(summary = "빌드 로그 조회", description = "특정 빌드의 로그를 조회합니다.")
    @PostMapping("/log")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildLogDto>> getBuildLog(
            @RequestBody BuildRequestDto.GetLogRequestDto dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildLog(dto)));
    }

    @Operation(summary = "빌드 실시간 로그 조회", description = "빌드 로그를 실시간으로 조회합니다.")
    @PostMapping(value = "/streamlog")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildStreamLogDto>> streamLog(
            @RequestBody BuildRequestDto.GetLogRequestDto dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getStreamLog(dto)));
    }
}
