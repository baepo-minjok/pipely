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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/jenkins/job")
@Tag(name = "Jenkins Job 관리", description = "Jenkins Job 관리 API")
public class JobController {

    private final PipelineService pipelineService;
    private final JenkinsInfoService jenkinsInfoService;

    @Operation(
            summary = "새 Job 생성",
            description = """
                        사용자가 지정한 설정(CreateDto)로 Jenkins에 새 Job을 생성합니다.
                        - JenkinsInfo ID, Script ID, Job 이름, 설명, 트리거 여부, 스케줄 등을 입력받습니다.
                        - 이미 동일한 이름의 Job이 존재하면 400 반환.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job 생성 성공"),
            @ApiResponse(responseCode = "400", description = "Dto 검증 오류 또는 중복된 Job 이름"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 JenkinsInfo Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "생성할 Job 정보"
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
            description = """
                        사용자가 지정한 설정(UpdateDto)로 Jenkins Job을 수정합니다.
                        - 이름 변경 시 중복 검사/삭제/재생성 처리
                        - Script, 설명, 트리거, 스케줄 등 변경 가능
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job 수정 성공"),
            @ApiResponse(responseCode = "400", description = "Dto 검증 오류 또는 중복된 Job 이름"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Job Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "수정할 Job 정보"
    )
    @PreAuthorize("@pipelineService.isOwner(#user, #requestDto.pipelineId)")
    @PutMapping
    public ResponseEntity<BaseResponse<String>> update(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid RequestDto.UpdateDto requestDto
    ) {
        pipelineService.updateJob(requestDto);
        return ResponseEntity.ok()
                .body(BaseResponse.success("update job success"));
    }


    @Operation(
            summary = "Job soft-delete",
            description = "지정한 Job을 소프트 삭제 처리합니다. (isDeleted=true, 삭제시간 기록)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Job Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @DeleteMapping
    public ResponseEntity<BaseResponse<String>> delete(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "삭제할 Job의 UUID", required = true, example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
            @RequestParam @NotNull UUID jobId
    ) {
        pipelineService.softDeletePipelineById(jobId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("soft-delete job success"));
    }


    @Operation(
            summary = "Job hard-delete",
            description = "지정한 Job을 완전 삭제합니다. (DB에서 영구 삭제)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Job Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @DeleteMapping("/hard")
    public ResponseEntity<BaseResponse<String>> hardDelete(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "삭제할 Job의 UUID", required = true, example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
            @RequestParam @NotNull UUID jobId
    ) {
        pipelineService.hardDeletePipelineById(jobId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("hard-delete job success"));
    }


    @Operation(
            summary = "Job 목록 조회",
            description = "JenkinsInfo에 연결된 모든 Job을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 JenkinsInfo Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #jenkinsInfoId)")
    @GetMapping
    public ResponseEntity<BaseResponse<List<ResponseDto.LightJobDto>>> getAll(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "조회할 JenkinsInfo UUID", required = true, example = "2c1edbe1-4e6a-420d-84cd-3ffb2b9d7c85")
            @RequestParam @NotNull UUID jenkinsInfoId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(pipelineService.getLightJobs(jenkinsInfoId)));
    }


    @Operation(
            summary = "Job 상세 조회",
            description = "Job Id로 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Job Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @GetMapping("/detail")
    public ResponseEntity<BaseResponse<ResponseDto.DetailJobDto>> getDetail(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "상세조회할 Job의 UUID", required = true, example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
            @RequestParam @NotNull UUID jobId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(pipelineService.getDetailJob(jobId)));
    }


    @Operation(
            summary = "삭제된 Job 목록 조회",
            description = "JenkinsInfo에 연결된 삭제된 Job 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제된 Job 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 JenkinsInfo Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #jenkinsInfoId)")
    @GetMapping("/deleted")
    public ResponseEntity<BaseResponse<List<ResponseDto.LightJobDto>>> getAllDeleted(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "조회할 JenkinsInfo UUID", required = true, example = "2c1edbe1-4e6a-420d-84cd-3ffb2b9d7c85")
            @RequestParam @NotNull UUID jenkinsInfoId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(pipelineService.getDeletedLightJobs(jenkinsInfoId)));
    }


    @Operation(
            summary = "파이프라인 버전 리스트 조회",
            description = "지정된 파이프라인 ID에 대해 모든 버전 기록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파이프라인 버전 리스트 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Pipeline Id")
    })
    @GetMapping("/pipelines/{id}/versions")

    public ResponseEntity<BaseResponse<List<ResponseDto.PipelineVersionDto>>> getVersions(
            @Parameter(description = "버전을 조회할 파이프라인 ID", example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
            @PathVariable UUID id) {
        List<ResponseDto.PipelineVersionDto> versions = pipelineService.getPipelineVersions(id);
        return ResponseEntity.ok().body(BaseResponse.success(versions));
    }


    @Operation(summary = "특정 파이프라인 버전 삭제", description = "파이프라인의 특정 버전 정보를 삭제합니다. latestVersion은 삭제할 수 없습니다.")
    @DeleteMapping("/{pipelineId}/version/{versionId}")
    public ResponseEntity<BaseResponse<String>> deletePipelineVersion(
            @Parameter(description = "삭제할 파이프라인이 속한 Pipeline의 UUID", example = "a2f1b6d3-1a9e-4a61-a5f5-91aef7e7b7ee")
            @PathVariable UUID pipelineId,
            @Parameter(description = "삭제할 파이프라인 버전 번호", example = "3")
            @PathVariable UUID versionId) {
        pipelineService.deletePipelineVersion(pipelineId, versionId);
        return ResponseEntity.ok().body(BaseResponse.success("delete pipeline success"));
    }


    @Operation(
            summary = "파이프라인 버전 스냅샷 생성",
            description = "파이프라인 버전 ID를 기반으로 새로운 스냅샷 버전을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스냅샷 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "404", description = "해당 버전 ID를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/version/{jobId}/snapshot")
    public ResponseEntity<BaseResponse<String>> snapshotVersion(
            @Parameter(description = "스냅샷 생성 대상이 될 파이프라인 버전 ID", required = true, example = "c2f4a511-3e55-4db5-b9b3-0123456789ab")
            @PathVariable UUID jobId,
            @Parameter(description = "생성할 스냅샷 이름", required = true, example = "My Snapshot")
            @RequestParam @NotBlank String snapshotName
    ) {
        pipelineService.snapshotVersion(jobId, snapshotName);
        return ResponseEntity.ok().body(BaseResponse.success("snapshot create success"));
    }


    @Operation(
            summary = "스냅샷 버전으로 변경",
            description = "선택한 스냅샷 버전으로 파이프라인을 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "404", description = "해당 파이프라인 또는 스냅샷 ID를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #pipelineId)")
    @PostMapping("/{pipelineId}/rollback")
    public ResponseEntity<BaseResponse<String>> rollbackToSnapshot(
            @Parameter(description = "변경 대상 파이프라인 ID", required = true, example = "f3a0e120-4a59-4bcf-b95f-abcdef123456")
            @PathVariable UUID pipelineId,
            @Parameter(description = "변경할 스냅샷 버전 ID", required = true, example = "a1b2c3d4-e5f6-7890-abcd-1234567890ef")
            @RequestParam @NotNull UUID snapshotVersionId
    ) {
        pipelineService.rollbackToSnapshot(pipelineId, snapshotVersionId);
        return ResponseEntity.ok().body(BaseResponse.success("snapshot update success"));
    }


    @Operation(
            summary = "삭제된 Job 복구",
            description = "Job Id로 소프트삭제된 Job을 복구합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job 복구 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Job Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @GetMapping("/restoration")
    public ResponseEntity<BaseResponse<String>> restorationJob(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "복구할 Job의 UUID", required = true, example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
            @RequestParam @NotNull UUID jobId
    ) {
        pipelineService.restorationJob(jobId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("restoration job success"));
    }
}
