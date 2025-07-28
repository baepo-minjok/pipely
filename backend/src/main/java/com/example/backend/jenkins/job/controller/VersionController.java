package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.jenkins.job.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/jenkins/job/version")
@Tag(name = "Jenkins Version 관리", description = "Jenkins Version 관리 API")
public class VersionController {

    private final VersionService versionService;
    private final PipelineService pipelineService;

    @Operation(
            summary = "특정 Job 버전 삭제",
            description = "Job의 특정 버전 정보를 삭제합니다. latestVersion은 삭제할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "검증 오류 또는 최신 버전 삭제 시도"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 버전 id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@versionService.isOwner(#user, #versionId)")
    @DeleteMapping("/{versionId}")
    public ResponseEntity<BaseResponse<String>> deleteJobVersion(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "삭제할 파이프라인 버전 Id", required = true, example = "c2f4a511-3e55-4db5-b9b3-0123456789ab")
            @PathVariable @NotNull UUID versionId) {
        versionService.deletePipelineVersion(versionId);
        return ResponseEntity.ok().body(BaseResponse.success("delete pipeline success"));
    }

    @Operation(
            summary = "Job 스냅샷 생성",
            description = "Job Id, 스냅샷 이름을 받아 새로운 스냅샷을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스냅샷 생성 성공"),
            @ApiResponse(responseCode = "400", description = "검증 오류 "),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 버전 id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    @PostMapping("/{jobId}/snapshot")
    public ResponseEntity<BaseResponse<String>> snapshotVersion(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "스냅샷을 생성할 Job Id", required = true, example = "c2f4a511-3e55-4db5-b9b3-0123456789ab")
            @PathVariable @NotNull UUID jobId,
            @Parameter(description = "생성할 스냅샷 이름", required = true, example = "My Snapshot")
            @RequestParam @NotBlank String snapshotName
    ) {
        versionService.snapshotVersion(jobId, snapshotName);
        return ResponseEntity.ok().body(BaseResponse.success("snapshot create success"));
    }

    @Operation(
            summary = "Job을 스냅샷 버전으로 롤백",
            description = "선택한 스냅샷 버전으로 Job을 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "Dto 검증 오류 또는 중복된 Job 이름"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 버전 Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@versionService.isOwner(#user, #versionId)")
    @PostMapping("/rollback")
    public ResponseEntity<BaseResponse<String>> rollbackToSnapshot(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "변경할 스냅샷 버전 ID", required = true, example = "a1b2c3d4-e5f6-7890-abcd-1234567890ef")
            @RequestParam @NotNull UUID versionId
    ) {
        versionService.rollbackToSnapshot(versionId);
        return ResponseEntity.ok().body(BaseResponse.success("snapshot update success"));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResponseDto.PipelineVersionDto>>> getAllVersions(
            @RequestParam @NotNull UUID jobId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(pipelineService.getLightVersionDtoList(jobId)));
    }

}
