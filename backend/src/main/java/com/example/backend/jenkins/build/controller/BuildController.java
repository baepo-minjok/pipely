package com.example.backend.jenkins.build.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.build.service.BuildService;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
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
@RequestMapping("/api/jenkins/build")
@Tag(name = "Jenkins Job의 Build 관리", description = "Jenkins 빌드를 담당하는 API입니다. Job 실행, 빌드 이력, 로그 조회 기능을 제공합니다.")
public class BuildController {

    private final BuildService buildService;
    private final PipelineService pipelineService;

    @Operation(
            operationId = "triggerStages",
            summary = "Job 빌드(스테이지 선택 실행/제외)",
            description = """
                      특정 Job을 빌드합니다. 요청의 stageBuilds를 통해 실행하거나 제외할 스테이지를 제어합니다.
                      - 자연어 예: "이 잡 빌드해", "테스트 스테이지만 제외하고 빌드"
                      - 필요 컨텍스트: jobId
                      - 부수효과: CI 파이프라인 실행
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "build,run,trigger,start"),
                            @ExtensionProperty(name = "nouns", value = "job build,pipeline build"),
                            @ExtensionProperty(name = "required_context", value = "jobId, stages(optional)")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실행 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 스테이지를 찾을 수 없음")
    })
    @PreAuthorize("@pipelineService.isOwner(#user, #dto.jobId)")
    @PostMapping("/stage/trigger")
    public ResponseEntity<BaseResponse<Integer>> Steps(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid BuildRequestDto.BuildStageRequestDto dto) {
        return ResponseEntity.ok(BaseResponse.success(buildService.triggerStages(dto)));
    }

    @Operation(
            operationId = "getLatestBuildHistory",
            summary = "빌드 이력(최신 1건)",
            description = """
                      특정 Job의 최신 빌드 결과를 반환합니다.
                      - 자연어 예: "마지막 빌드 상태", "최근 빌드 결과"
                      - 필요 컨텍스트: jobId
                      - read-only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "get,show,fetch,view"),
                            @ExtensionProperty(name = "nouns", value = "latest build,build info"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "Job(파이프라인) 없음")
    })
    @GetMapping("/history/latest")
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildInfo>> getLatestHistory(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam @NotNull UUID jobId) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getLastBuildStatus(jobId)));
    }

    @Operation(
            operationId = "getAllBuildHistory",
            summary = "빌드 이력(전체)",
            description = """
                      특정 Job의 빌드 이력 전체 목록을 반환합니다.
                      - 자연어 예: "이력 보여줘", "전체 빌드 리스트"
                      - 필요 컨텍스트: jobId
                      - read-only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "list,show,get,fetch"),
                            @ExtensionProperty(name = "nouns", value = "build history,build list"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "Job(파이프라인) 없음")
    })
    @GetMapping("/history/all")
    @PreAuthorize("@pipelineService.isOwner(#user, #jobId)")
    public ResponseEntity<BaseResponse<List<BuildResponseDto.BuildInfo>>> getAllHistory(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam @NotNull UUID jobId) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildHistory(jobId)));
    }

    @Operation(
            operationId = "setBuildStatus",
            summary = "Job 빌드상태 수동 변경",
            description = """
                      StatusDto에 따라 빌드 상태를 BUILD_SUCCESS 또는 BUILD_FAILURE로 변경합니다.
                      - 자연어 예: "이 잡 상태 성공으로 바꿔", "실패로 마킹"
                      - 필요 컨텍스트: jobId, success
                      - 부수효과: 상태 변경
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "set,mark,change,update"),
                            @ExtensionProperty(name = "nouns", value = "build status,status"),
                            @ExtensionProperty(name = "required_context", value = "jobId,success")
                    })
            }
    )
    @PostMapping("/status")
    public ResponseEntity<BaseResponse<Boolean>> setStatus(
            @RequestBody @Valid RequestDto.StatusDto dto
    ) {
        pipelineService.setState(dto);
        return ResponseEntity.ok()
                .body(BaseResponse.success(true));
    }

    @Operation(
            operationId = "getCurrentBuildNumber",
            summary = "최신 빌드번호 조회",
            description = """
                      해당 Job의 최신 빌드 번호를 반환합니다.
                      - 자연어 예: "현재 빌드번호 알려줘"
                      - 필요 컨텍스트: jobId
                      - read-only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "get,show,fetch"),
                            @ExtensionProperty(name = "nouns", value = "build number,current build"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
    )
    @GetMapping("/buildNumber")
    public ResponseEntity<BaseResponse<Integer>> getBuildNumber(
            @RequestParam UUID jobId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(buildService.getCurrentBuildNumber(jobId)));
    }

    @Operation(
            operationId = "stopBuild",
            summary = "빌드 중단",
            description = """
                      해당 Job의 진행 중인 빌드를 중단합니다.
                      - 자연어 예: "빌드 중지해", "현재 빌드 멈춰"
                      - 필요 컨텍스트: jobId
                      - 부수효과: 실행 중 파이프라인 취소
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "stop,abort,cancel,terminate"),
                            @ExtensionProperty(name = "nouns", value = "build,job"),
                            @ExtensionProperty(name = "required_context", value = "jobId")
                    })
            }
    )
    @PostMapping("/stop")
    public ResponseEntity<BaseResponse<Boolean>> stop(
            @RequestParam UUID jobId
    ) {
        buildService.stopBuild(jobId);
        return ResponseEntity.ok()
                .body(BaseResponse.success(true));
    }

    @Operation(
            operationId = "sendBuildLogToUserQueue",
            summary = "현재 빌드상황 푸시(웹소켓)",
            description = """
                      현재 빌드 중이라면 빌드 현황(로그, 진행률 등)을 사용자 개인 큐(/user/queue/...)로 푸시합니다.
                      - 자연어 예: "실시간 로그 보내줘"
                      - 필요 컨텍스트: jobId, buildNumber
                      - 부수효과: 웹소켓 메시지 전송
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "send,stream,push"),
                            @ExtensionProperty(name = "nouns", value = "live log,build progress"),
                            @ExtensionProperty(name = "required_context", value = "jobId,buildNumber")
                    })
            }
    )
    @GetMapping("/sendLog")
    public ResponseEntity<Void> test(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestParam UUID jobId,
            @RequestParam Integer buildNumber
    ) {
        buildService.sendLog(jobId, buildNumber, user.getEmail());
        return ResponseEntity.ok().build();
    }
}
