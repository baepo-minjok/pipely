package com.example.backend.jenkins.job.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.FreeStyleResponseDto.DetailFreeStyleDto;
import com.example.backend.jenkins.job.model.dto.FreeStyleResponseDto.DetailHistoryDto;
import com.example.backend.jenkins.job.model.dto.FreeStyleResponseDto.LightFreeStyleDto;
import com.example.backend.jenkins.job.model.dto.FreeStyleResponseDto.LightHistoryDto;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.service.FreeStyleJobService;
import com.example.backend.jenkins.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "FreeStyle Job API", description = "Jenkins Freestyle 잡 생성, 수정, 삭제 및 이력 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/job")
public class JobController {

    private final FreeStyleJobService freeStyleJobService;
    private final JobService jobService;

    @Operation(
            summary = "새 Job 생성",
            description = "사용자가 지정한 설정(CreateDto)을 기반으로 Jenkins에 Freestyle 잡을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FreeStyle Job 생성 성공"),
            @ApiResponse(responseCode = "400", description = "Dto 검증 오류"),
            @ApiResponse(responseCode = "404", description = "잘못된 JenkinsInfo Id"),
            @ApiResponse(responseCode = "500", description = "Jenkins 서버 문제로 인한 실패")
    })
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(
            @RequestBody @Valid RequestDto.CreateDto dto
    ) {
        jobService.createJob(dto);
        return ResponseEntity.ok()
                .body(BaseResponse.success("create job success"));
    }

    @PostMapping("/generate/script")
    public ResponseEntity<BaseResponse<String>> generateScript(
            @RequestBody @Valid RequestDto.GenerateScriptDto dto
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(jobService.generateScript(dto)));
    }

    @Operation(
            summary = "기존 Job 수정",
            description = "사용자가 지정한 설정(UpdateDto)을 기반으로 Jenkins에 Freestyle 잡을 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job 수정 성공"),
            @ApiResponse(responseCode = "400", description = "Dto 검증 오류"),
            @ApiResponse(responseCode = "404", description = "잘못된 job Id"),
            @ApiResponse(responseCode = "500", description = "Jenkins 서버 문제로 인한 실패")
    })
    @PutMapping
    public ResponseEntity<BaseResponse<String>> update(
            @RequestBody @Valid RequestDto.UpdateDto requestDto
    ) {

        jobService.updateJob(requestDto);

        return ResponseEntity.ok()
                .body(BaseResponse.success("update freestyle success"));
    }

    @Operation(
            summary = "FreeStyle 잡 삭제",
            description = "지정된 ID의 Freestyle 잡을 소프트 삭제 처리합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FreeStyle Job 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 FreeStyle Id")
    })
    @DeleteMapping("/freeStyle")
    public ResponseEntity<BaseResponse<String>> deleteFreeStyle(
            @Parameter(description = "삭제할 FreeStyle 잡의 UUID", required = true)
            @RequestParam UUID id
    ) {
        freeStyleJobService.deleteById(id);
        return ResponseEntity.ok()
                .body(BaseResponse.success("delete freestyle success"));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<LightFreeStyleDto>>> getAllFreeStyle(
            @Parameter(description = "JenkinsInfo의 UUID", required = true)
            @RequestParam UUID infoId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(freeStyleJobService.getAllLightFreeStyle(infoId)));
    }

    @PostMapping("/{id}")
    public ResponseEntity<BaseResponse<DetailFreeStyleDto>> getFreeStyleById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(freeStyleJobService.getDetailFreeStyleById(id)));
    }

    @Operation(
            summary = "Freestyle 잡 이력 목록 조회",
            description = "지정된 FreeStyle 잡의 이력 목록(LightHistoryDto)을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FreeStyle 이력 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 FreeStyle Id")
    })
    @GetMapping("/freeStyle/history")
    public ResponseEntity<BaseResponse<List<LightHistoryDto>>> getFreeStyleHistory(
            @Parameter(description = "조회할 FreeStyle 잡의 UUID", required = true)
            @RequestParam UUID id
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(freeStyleJobService.getLightHistory(id)));
    }

    @Operation(
            summary = "단일 FreeStyle 이력 상세 조회",
            description = "이력 ID를 통해 해당 FreeStyleHistory의 상세 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "단일 FreeStyleHistory 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 FreeStyleHistory Id")
    })
    @GetMapping("/freeStyle/history/{id}")
    public ResponseEntity<BaseResponse<DetailHistoryDto>> getFreeStyleHistoryById(
            @Parameter(description = "조회할 FreeStyleHistory의 UUID", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(freeStyleJobService.getFreeStyleHistoryById(id)));
    }

    @Operation(
            summary = "Freestyle 잡 롤백",
            description = "지정된 이력 ID를 기반으로 Freestyle 잡을 해당 버전으로 롤백합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "단일 FreeStyleHistory 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 FreeStyleHistory Id 또는 연결 실패"),
            @ApiResponse(responseCode = "500", description = "Jenkins 서버 문제로 인한 실패")
    })
    @GetMapping("/freeStyle/rollBack")
    public ResponseEntity<BaseResponse<String>> rollBack(
            @Parameter(description = "롤백할 FreeStyleHistory의 UUID", required = true)
            @RequestParam UUID id
    ) {
        freeStyleJobService.rollBack(id);
        return ResponseEntity.ok()
                .body(BaseResponse.success("roll back success"));
    }
}
