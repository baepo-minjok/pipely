package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/job")
public class JobController {

    private final PipelineService pipelineService;
    private final JenkinsInfoService jenkinsInfoService;

    @Operation(
            summary = "새 Job 생성",
            description = "사용자가 지정한 설정(CreateDto)을 기반으로 Jenkins에 Freestyle 잡을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FreeStyle Job 생성 성공"),
            @ApiResponse(responseCode = "400", description = "Dto 검증 오류 혹은 이미 존재하는 이름입니다."),
            @ApiResponse(responseCode = "404", description = "잘못된 JenkinsInfo Id"),
            @ApiResponse(responseCode = "500", description = "Jenkins 서버 문제로 인한 실패")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = RequestDto.CreateDto.class))
    )
    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #requestDto.infoId)")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid RequestDto.CreateDto requestDto
    ) {
        pipelineService.createJob(requestDto);
        return ResponseEntity.ok()
                .body(BaseResponse.success("create job success"));
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
    @PreAuthorize("@pipelineService.isOwner(#user, #requestDto.pipelineId)")
    @PutMapping
    public ResponseEntity<BaseResponse<String>> update(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid RequestDto.UpdateDto requestDto
    ) {

        pipelineService.updateJob(requestDto);

        return ResponseEntity.ok()
                .body(BaseResponse.success("update freestyle success"));
    }

    @Operation(
            summary = " Job soft-delete",
            description = "지정된 ID의 Job을 soft-delete 처리합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 Job Id")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @DeleteMapping
    public ResponseEntity<BaseResponse<String>> delete(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "삭제할 Job의 UUID", required = true)
            @RequestParam UUID jobId
    ) {
        pipelineService.softDeletePipelineById(jobId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("delete freestyle success"));
    }

    @Operation(
            summary = " Job hard-delete",
            description = "지정된 ID의 Job을 hard-delete 처리합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 Job Id")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @DeleteMapping("/hard")
    public ResponseEntity<BaseResponse<String>> hardDelete(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "삭제할 Job의 UUID", required = true)
            @RequestParam UUID jobId
    ) {
        pipelineService.hardDeletePipelineById(jobId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("delete freestyle success"));
    }

    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #jenkinsInfoId)")
    @GetMapping
    public ResponseEntity<BaseResponse<List<ResponseDto.LightJobDto>>> getAll(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "조회할 jenkins info의 UUID", required = true)
            @RequestParam UUID jenkinsInfoId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(pipelineService.getLightJobs(jenkinsInfoId)));
    }

    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @GetMapping("/detail")
    public ResponseEntity<BaseResponse<ResponseDto.DetailJobDto>> getDetail(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "조회할 job의 UUID", required = true)
            @RequestParam UUID jobId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(pipelineService.getDetailJob(jobId)));
    }

    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #jenkinsInfoId)")
    @GetMapping("/deleted")
    public ResponseEntity<BaseResponse<List<ResponseDto.LightJobDto>>> getAllDeleted(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "조회할 jenkins info의 UUID", required = true)
            @RequestParam UUID jenkinsInfoId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(pipelineService.getDeletedLightJobs(jenkinsInfoId)));
    }

    @Operation(
            summary = "파이프라인 버전 리스트 조회",
            description = "지정된 파이프라인 ID에 대해 모든 버전 기록을 반환합니다."
    )
    @GetMapping("/pipelines/{id}/versions")
    public ResponseEntity<BaseResponse<List<ResponseDto.PipelineVersionDto>>> getVersions(
            @Parameter(description = "버전을 조회할 파이프라인 ID", example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
            @PathVariable UUID id) {
        List<ResponseDto.PipelineVersionDto> versions = pipelineService.getPipelineVersions(id);
        return ResponseEntity.ok().body(BaseResponse.success(versions));
    }

    @Operation(summary = "특정 파이프라인 버전 삭제", description = "파이프라인의 특정 버전 정보를 삭제합니다. latestVersion은 삭제할 수 없습니다.")
    @DeleteMapping("/{pipelineId}/version/{version}")
    public ResponseEntity<BaseResponse<String>> deletePipelineVersion(
            @Parameter(description = "삭제할 파이프라인이 속한 Pipeline의 UUID", example = "a2f1b6d3-1a9e-4a61-a5f5-91aef7e7b7ee")
            @PathVariable UUID pipelineId,
            @Parameter(description = "삭제할 파이프라인 버전 번호", example = "3")
            @PathVariable Integer version) {
        pipelineService.deletePipelineVersion(pipelineId, version);
        return ResponseEntity.ok().body(BaseResponse.success("delete pipeline success"));
    }


    @Operation(summary = "최신 버전 롤백", description = "현재 최신 파이프라인 버전을 삭제하고, 직전 버전으로 롤백합니다.")
    @PutMapping("/{pipelineId}/rollback/previous")
    public ResponseEntity<BaseResponse<String>> rollbackToPreviousVersion(
            @Parameter(description = "파이프라인 ID", required = true) @PathVariable UUID pipelineId) {

        pipelineService.rollbackToPreviousVersion(pipelineId);
        return ResponseEntity.ok(BaseResponse.success("Rollback to previous version successful"));
    }

    @Operation(summary = "특정 버전 롤백", description = "지정된 파이프라인 버전으로 롤백합니다. 기존 버전으로 최신 버전 값을 바꿉니다.")
    @PutMapping("/{pipelineId}/rollback/{version}")
    public ResponseEntity<BaseResponse<String>> rollbackToSpecificVersion(
            @Parameter(description = "파이프라인 ID", required = true) @PathVariable UUID pipelineId,
            @Parameter(description = "롤백 대상 버전", required = true) @PathVariable int version) {

        pipelineService.rollbackToSpecificVersion(pipelineId, version);
        return ResponseEntity.ok(BaseResponse.success("Rollback to version " + version + " successful"));
    }


}
