package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.jenkins.job.service.ScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final ScriptService scriptService;

    @Operation(
            operationId = "createJob",
            summary = "Create a new Jenkins Job (생성/만들다/추가)",
            description = """
                      JenkinsInfo에 연결된 새 Job을 생성합니다.
                      - 자연어 예: "이 Jenkins에서 새 잡 만들어줘", "파이프라인 추가", "잡 생성"
                      - 필요 컨텍스트: jenkinsInfoId, job name, script spec
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "create,make,add,generate,provision"),
                            @ExtensionProperty(name = "nouns", value = "job,pipeline,jenkins job,ci job"),
                            @ExtensionProperty(name = "required_context", value = "jenkinsInfoId, jobName, script")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Job 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "success",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": "create job success",
                                                      "error": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dto 검증 오류 또는 중복된 Job 이름",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "duplicate-name",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": { "code": "JOB_NAME_DUPLICATED", "message": "Job name already exists" }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 JenkinsInfo Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #req.job.infoId)")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid RequestDto.CreateJobRequest req
    ) {
        Script script = scriptService.generateScript(req.getScript());
        pipelineService.createJob(req.getJob(), script);
        return ResponseEntity.ok()
                .body(BaseResponse.success("create job success"));
    }


    @Operation(
            operationId = "updateJob",
            summary = "기존 Job 수정 (수정/변경/업데이트)",
            description = """
                        기존 Job의 메타데이터/스크립트를 수정합니다.
                        - 자연어 예: "이 잡 스케줄 바꿔줘", "스크립트 브랜치를 develop로 변경"
                        - 필요 컨텍스트: jobId
                        - 부수효과: updates resource
                        - 권한: owner only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "update,edit,modify,change,patch"),
                            @ExtensionProperty(name = "nouns", value = "job,pipeline"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job 수정 성공"),
            @ApiResponse(responseCode = "400", description = "Dto 검증 오류 또는 중복된 Job 이름"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Job Id"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #req.job.pipelineId)")
    @PutMapping
    public ResponseEntity<BaseResponse<String>> update(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid RequestDto.UpdateJobRequest req
    ) {
        Script script = scriptService.generateScript(req.getScript());
        pipelineService.updateJob(req.getJob(), script);
        return ResponseEntity.ok()
                .body(BaseResponse.success("update job success"));
    }


    @Operation(
            operationId = "deleteJobSoft",
            summary = "Job soft-delete (삭제/숨김/휴지통)",
            description = """
                        지정한 Job을 소프트 삭제 처리합니다. (복구 가능)
                        - 자연어 예: "이 잡 삭제해", "휴지통으로 보내"
                        - 필요 컨텍스트: jobId
                        - 부수효과: recoverable delete
                        - 권한: owner only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "delete,remove,soft-delete,trash"),
                            @ExtensionProperty(name = "nouns", value = "job,pipeline"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
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
            operationId = "deleteJobHard",
            summary = "Job hard-delete (영구 삭제/되돌릴 수 없음)",
            description = """
                        지정한 Job을 완전 삭제합니다. (DB에서 영구 삭제, 복구 불가)
                        - 자연어 예: "이 잡 완전 삭제해", "영구 삭제"
                        - 필요 컨텍스트: jobId
                        - 부수효과: irreversible delete
                        - 권한: owner only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "hard-delete,permanently delete,erase,destroy"),
                            @ExtensionProperty(name = "nouns", value = "job,pipeline"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
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
            operationId = "listJobsByJenkinsInfo",
            summary = "Job 목록 조회 (리스트/나열/보여줘)",
            description = """
                        JenkinsInfo에 연결된 삭제되지 않은 모든 Job을 조회합니다.
                        - 자연어 예: "이 젠킨스에 잡들 보여줘", "job 리스트"
                        - 필요 컨텍스트: jenkinsInfoId
                        - read-only
                        - 권한: owner only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "list,show,get,fetch"),
                            @ExtensionProperty(name = "nouns", value = "jobs,job list,pipelines"),
                            @ExtensionProperty(name = "required_context", value = "jenkinsInfoId")
                    })
            }
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
            operationId = "getJobDetail",
            summary = "Job 상세 조회 (상세/정보 보여줘)",
            description = """
                        특정 Job의 상세 정보를 반환합니다.
                        - 자연어 예: "이 잡 상세 보여줘", "잡 설정 확인"
                        - 필요 컨텍스트: jobId
                        - read-only
                        - 권한: owner only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "get,show,view,fetch,look up,see"),
                            @ExtensionProperty(name = "nouns", value = "job detail,job info,pipeline detail"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
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
            operationId = "listDeletedJobs",
            summary = "삭제된 Job 목록 조회 (휴지통/삭제됨)",
            description = """
                        JenkinsInfo의 소프트 삭제된 Job 목록을 조회합니다.
                        - 자연어 예: "삭제된 잡들 보여줘", "휴지통 목록"
                        - 필요 컨텍스트: jenkinsInfoId
                        - read-only
                        - 권한: owner only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "list,show,get,fetch"),
                            @ExtensionProperty(name = "nouns", value = "deleted jobs,trashed jobs"),
                            @ExtensionProperty(name = "required_context", value = "jenkinsInfoId")
                    })
            }
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
            operationId = "restoreJob",
            summary = "삭제된 Job 복구 (복원/되살리기)",
            description = """
                        소프트 삭제된 Job을 복구합니다.
                        - 자연어 예: "이 잡 복구해", "되살려줘"
                        - 필요 컨텍스트: jobId
                        - 부수효과: restore resource
                        - 권한: owner only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "restore,recover,undelete"),
                            @ExtensionProperty(name = "nouns", value = "job,pipeline"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
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
