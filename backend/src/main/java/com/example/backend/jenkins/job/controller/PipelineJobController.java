package com.example.backend.jenkins.job.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.pipeline.PipelineRequestDto;
import com.example.backend.jenkins.job.service.PipelineJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Pipeline Job API", description = "Jenkins pipeline 잡 생성, 수정, 삭제 및 이력 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/job")
public class PipelineJobController {
    private final PipelineJobService pipelineJobService;

    @Operation(
            summary = "새 Pipeline 잡 생성",
            description = "사용자가 지정한 설정(CreatePipelineDto)을 기반으로 Jenkins에 PipelineScript 잡을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PipelineScript Job 생성 성공"),
            @ApiResponse(responseCode = "400", description = "Dto 검증 오류"),
            @ApiResponse(responseCode = "404", description = "잘못된 JenkinsInfo Id"),
            @ApiResponse(responseCode = "500", description = "Jenkins 서버 문제로 인한 실패")
    })
    @PostMapping("/create/pipeline-script")
    public ResponseEntity<BaseResponse<String>> createPipeline(
            @RequestBody @Valid PipelineRequestDto.CreatePipelineDto dto
    ) {
        pipelineJobService.createPipelineScriptJob(dto);
        return ResponseEntity.ok()
                .body(BaseResponse.success("create pipeline script job success"));
    }
}
