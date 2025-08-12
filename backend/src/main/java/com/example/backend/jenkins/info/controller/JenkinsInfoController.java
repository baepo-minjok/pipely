package com.example.backend.jenkins.info.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.CreateDto;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.InfoDto;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.UpdateDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.DetailInfoDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.LightInfoDto;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Jenkins Info API", description = "사용자의 Jenkins 연동 정보를 관리하기 위한 API 그룹입니다.")
@RestController
@RequestMapping("/api/jenkins/info")
@RequiredArgsConstructor
public class JenkinsInfoController {

    private final JenkinsInfoService jenkinsInfoService;

    @Operation(
            operationId = "createJenkinsInfo",
            summary = "Jenkins 정보 등록 (추가/생성)",
            description = """
                      인증된 사용자가 Jenkins 서버 접속 정보를 새로 등록합니다.
                      - 자연어 예: "Jenkins 서버 추가해", "젠킨스 정보 등록"
                      - 필요 컨텍스트: 이름, 서버 URL, Jenkins ID, API 토큰(설명은 선택)
                      - 결과: 생성 후 성공 메시지 반환
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "create,add,register,save"),
                            @ExtensionProperty(name = "nouns", value = "jenkins info,jenkins server,connection"),
                            @ExtensionProperty(name = "required_context", value = "name,serverUrl,jenkinsId,apiToken")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필드 누락 또는 형식 오류)"),
            @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자")
    })
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid CreateDto request) {
        jenkinsInfoService.createJenkinsInfo(user, request);
        return ResponseEntity.ok()
                .body(BaseResponse.success("create jenkins info success"));
    }

    @Operation(
            operationId = "updateJenkinsInfo",
            summary = "Jenkins 정보 수정 (변경/업데이트)",
            description = """
                      기존에 등록된 Jenkins 정보를 업데이트합니다.
                      - 자연어 예: "젠킨스 URL 바꿔줘", "토큰 갱신"
                      - 필요 컨텍스트: infoId
                      - 결과: 성공 메시지 반환
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "update,edit,modify,change"),
                            @ExtensionProperty(name = "nouns", value = "jenkins info,jenkins server,connection"),
                            @ExtensionProperty(name = "required_context", value = "infoId")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필드 누락 또는 형식 오류)"),
            @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정보")
    })
    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #request.infoId)")
    @PutMapping
    public ResponseEntity<BaseResponse<String>> update(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid UpdateDto request
    ) {
        jenkinsInfoService.updateJenkinsInfo(request);
        return ResponseEntity.ok()
                .body(BaseResponse.success("Jenkins Info update Success"));
    }

    @Operation(
            operationId = "listJenkinsInfos",
            summary = "Jenkins 정보 리스트 조회",
            description = """
                      현재 로그인된 사용자가 등록한 모든 Jenkins 정보를 간략 형태로 반환합니다.
                      - 자연어 예: "등록한 젠킨스들 보여줘", "모든 Jenkins 목록"
                      - read-only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "list,show,get,fetch"),
                            @ExtensionProperty(name = "nouns", value = "jenkins infos,servers,connections"),
                            @ExtensionProperty(name = "required_context", value = "")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<LightInfoDto>>> getAll(
            @AuthenticationPrincipal(expression = "userEntity") Users user
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(jenkinsInfoService.getAllLightDtoByUser(user)));
    }

    @Operation(
            operationId = "getJenkinsInfoDetail",
            summary = "Jenkins 정보 상세 조회",
            description = """
                      특정 Jenkins 정보 ID를 기반으로 상세 정보를 조회합니다.
                      - 자연어 예: "이 젠킨스 상세 보여줘"
                      - 필요 컨텍스트: infoId
                      - read-only
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "get,show,view,fetch,look up"),
                            @ExtensionProperty(name = "nouns", value = "jenkins info detail,server detail"),
                            @ExtensionProperty(name = "required_context", value = "infoId")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필드 누락 또는 형식 오류)"),
            @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정보")
    })

    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #dto.infoId)")
    @PostMapping
    public ResponseEntity<BaseResponse<DetailInfoDto>> getById(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid InfoDto dto
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(jenkinsInfoService.getDetailInfoById(dto.getInfoId())));
    }

    @Operation(
            operationId = "deleteJenkinsInfo",
            summary = "Jenkins 정보 삭제 (영구)",
            description = """
                      Jenkins 정보 ID를 받아 해당 정보를 삭제합니다(복구 불가).
                      - 자연어 예: "이 젠킨스 삭제해", "서버 제거"
                      - 필요 컨텍스트: infoId
                      - 부수효과: irreversible delete
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "delete,remove,erase"),
                            @ExtensionProperty(name = "nouns", value = "jenkins info,jenkins server"),
                            @ExtensionProperty(name = "required_context", value = "infoId")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정보")
    })
    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #infoId)")
    @DeleteMapping("/{infoId}")
    public ResponseEntity<BaseResponse<String>> delete(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @Parameter(description = "삭제할 Jenkins 정보의 고유 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID infoId
    ) {
        jenkinsInfoService.deleteJenkinsInfo(infoId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("delete Jenkins Info Success"));
    }

    @Operation(
            operationId = "verifyJenkinsInfoAccess",
            summary = "Jenkins 정보 접근 검증 (연결 테스트)",
            description = """
                      등록된 Jenkins 정보로 실제 접근 테스트를 수행하여 유효성을 검증합니다.
                      - 자연어 예: "연결 테스트 해줘", "접속 확인"
                      - 필요 컨텍스트: infoId (또는 자격/URL이 담긴 InfoDto)
                      - 결과: 성공/실패 메시지
                    """,
            extensions = {
                    @Extension(name = "x-intents", properties = {
                            @ExtensionProperty(name = "verbs", value = "verify,check,test,validate"),
                            @ExtensionProperty(name = "nouns", value = "connection,jenkins info"),
                            @ExtensionProperty(name = "required_context", value = "infoId or credentials")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검증 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (형식 오류)"),
            @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "잘못된 Jenkins 정보로 인한 연결 실패"),
            @ApiResponse(responseCode = "500", description = "Jenkins 서버 오류")
    })
    @PostMapping("/verification")
    public ResponseEntity<BaseResponse<String>> getVerification(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid InfoDto dto
    ) {
        if (dto.getInfoId() != null) {
            jenkinsInfoService.verificationJenkinsInfo(dto.getInfoId());
        } else {
            jenkinsInfoService.verificationJenkinsInfo(dto);
        }
        return ResponseEntity.ok()
                .body(BaseResponse.success("verify success"));
    }
}
