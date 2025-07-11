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
            summary = "Jenkins 정보 등록",
            description = "인증된 사용자가 Jenkins 서버 접속 정보를 새로 등록합니다.\n" +
                    "- 필수 입력값: 서버 URL, Jenkins ID, API 토큰 등\n" +
                    "- 등록 완료 시 저장된 정보 ID를 반환하지는 않으며, 상태 코드로 성공 여부만 전달합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필드 누락 또는 형식 오류)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
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
            summary = "Jenkins 정보 수정",
            description = "기존에 등록된 Jenkins 정보를 업데이트합니다.\n" +
                    "- 수정 가능한 항목: 서버 URL, 사용자 이름, API 토큰 등\n" +
                    "- 해당 정보가 존재하지 않거나 권한이 없으면 예외 발생\n" +
                    "- 성공 시 변경된 정보를 저장하고 성공 메시지를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (ID 누락 또는 형식 오류)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정보"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
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
            summary = "Jenkins 정보 리스트 조회",
            description = "현재 로그인된 사용자가 등록한 모든 Jenkins 정보를 조회하여 간략 형태(LightInfoDto)로 반환합니다.\n" +
                    "- 페이징 또는 필터링은 지원하지 않으며, 전체 목록을 반환\n" +
                    "- 반환된 DTO에는 최소 정보(ID, 서버 URL, 닉네임)가 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<LightInfoDto>>> getAll(
            @AuthenticationPrincipal(expression = "userEntity") Users user
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(jenkinsInfoService.getAllLightDtoByUser(user)));
    }

    @Operation(
            summary = "Jenkins 정보 상세 조회",
            description = "특정 Jenkins 정보 ID를 기반으로 상세 정보를 조회합니다.\n" +
                    "- 반환된 DetailInfoDto에는 서버 설정, 인증 토큰 만료일 등 모든 필드 포함\n" +
                    "- 잘못된 ID 또는 권한 없는 접근 시 오류 응답 발생합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (ID 누락 또는 형식 오류)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정보"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
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
            summary = "Jenkins 정보 삭제",
            description = "지정된 Jenkins 정보 ID를 통해 해당 정보를 삭제합니다.\n" +
                    "- 삭제된 정보는 복구 불가\n" +
                    "- 권한이 없는 사용자는 접근 불가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정보"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
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
            summary = "Jenkins 정보 접근 검증",
            description = "등록된 Jenkins 정보로 실제 접근 테스트를 수행하여 유효성을 검증합니다.\n" +
                    "- 성공 시 연결 테스트가 완료되었다는 메시지를 반환\n" +
                    "- 네트워크 오류, 인증 오류 등 발생 시 예외 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검증 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (형식 오류)"),
            @ApiResponse(responseCode = "404", description = "잘못된 Jenkins 정보로 인한 연결 실패"),
            @ApiResponse(responseCode = "500", description = "Jenkins 서버 오류")
    })
    @PreAuthorize("@jenkinsInfoService.isOwner(#user, #dto.infoId)")
    @PostMapping("/verification")
    public ResponseEntity<BaseResponse<String>> getVerification(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @RequestBody @Valid InfoDto dto
    ) {
        jenkinsInfoService.verificationJenkinsInfo(dto.getInfoId());
        return ResponseEntity.ok()
                .body(BaseResponse.success("verify success"));
    }
}
