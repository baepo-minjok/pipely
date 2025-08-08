package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.dto.RequestDto.CreateDto;
import com.example.backend.jenkins.job.model.dto.RequestDto.UpdateDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/jenkins/job")
@Tag(name = "Jenkins Job 관리", description = "Jenkins Job 관리 API")
public class JobController {

    private final PipelineService pipelineService;
    private final JenkinsInfoService jenkinsInfoService;

    @Operation(
            summary = "새 Job 생성",
            description = """
                        사용자가 지정한 설정에 맞게 Jenkins에 새 Job을 생성합니다.
                        script는 script 생성 api로 먼저 생성해 id를 보내야합니다.
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
    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #requestDto.infoId)")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid CreateDto requestDto
    ) {
        pipelineService.createJob(requestDto);
        return ResponseEntity.ok()
                .body(BaseResponse.success("create job success"));
    }


    @Operation(
            summary = "기존 Job 수정",
            description = """
                        사용자가 지정한 설정에 맞게 Jenkins Job을 수정합니다.
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
    @PreAuthorize("@pipelineService.isOwner(#user, #requestDto.pipelineId)")
    @PutMapping
    public ResponseEntity<BaseResponse<String>> update(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid UpdateDto requestDto
    ) {
        pipelineService.updateJob(requestDto);
        return ResponseEntity.ok()
                .body(BaseResponse.success("update job success"));
    }


    @Operation(
            summary = "Job soft-delete",
            description = "지정한 Job을 소프트 삭제 처리합니다."
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
            description = "JenkinsInfo에 연결된 삭제되지 않은 모든 Job을 조회합니다."
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
            description = "해당 Job의 상세 정보를 조회합니다."
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
            description = "JenkinsInfo의 삭제된 Job 목록을 조회합니다."
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
            summary = "삭제된 Job 복구",
            description = "소프트 삭제된 Job을 복구합니다."
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
