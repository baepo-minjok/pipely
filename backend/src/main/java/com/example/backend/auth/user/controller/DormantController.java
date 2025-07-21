package com.example.backend.auth.user.controller;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.ReactivateRequestDto;
import com.example.backend.auth.user.service.DormantTokenService;
import com.example.backend.exception.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dormant API", description = "휴면 계정 재활성화 관련 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/reactive")
public class DormantController {

    private final DormantTokenService dormantTokenService;
    private final EmailService emailService;

    @Operation(
            summary = "재활성화 이메일 발송",
            description = """
                    휴면 상태(DORMANT)의 계정을 다시 활성화할 수 있도록 재활성화 링크를 이메일로 발송합니다.
                    
                    - 요청한 이메일이 시스템에 존재하는 경우, 해당 사용자에게 토큰이 포함된 URL이 전송됩니다.
                    - 이 토큰은 일정 시간 동안만 유효하며, 만료되면 재요청이 필요합니다.
                    - 보안을 위해 존재하지 않는 이메일에 대해서도 성공 응답을 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재활성화 이메일 전송 성공"),
            @ApiResponse(responseCode = "400", description = "이메일 형식이 잘못되었거나 필수값 누락")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<String>> sendReactiveEmail(
            @Parameter(description = "재활성화 이메일을 수신할 사용자 이메일", example = "user@example.com", required = true)
            @RequestParam @NotBlank(message = "이메일은 필수입니다.") String email
    ) {
        emailService.sendDormantNotificationEmail(email);
        return ResponseEntity.ok(BaseResponse.success("send reactive email success"));
    }

    @Operation(
            summary = "계정 재활성화",
            description = """
                    발급된 재활성화 토큰을 이용해 휴면 계정을 정상 상태(ACTIVE)로 전환합니다.
                    
                    - 클라이언트는 이메일로 전달받은 토큰을 본 API에 제출합니다.
                    - 유효한 토큰이라면 사용자 계정의 상태가 ACTIVE로 변경되며, 이후 정상 로그인 가능합니다.
                    - 토큰은 재사용할 수 없으며, 처리 후 즉시 삭제됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "토큰이 유효하지 않거나 만료됨")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<String>> reactive(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "재활성화 요청 DTO. 이메일로 전달받은 토큰을 포함합니다.",
                    required = true
            )
            @RequestBody @Valid ReactivateRequestDto request
    ) {
        Users user = dormantTokenService.validateAndGetUserByToken(request.getToken());

        dormantTokenService.deleteTokensByUser(user);

        return ResponseEntity.ok()
                .body(BaseResponse.success("reactive success"));
    }
}
