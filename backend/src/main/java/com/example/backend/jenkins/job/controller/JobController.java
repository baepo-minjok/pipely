package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

}
