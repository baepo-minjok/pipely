package com.example.backend.auth.user.controller;

import com.example.backend.auth.user.model.dto.PasswordResetDto;
import com.example.backend.auth.user.service.PasswordResetService;
import com.example.backend.exception.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Password Reset API", description = "비밀번호 변경 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "비밀번호 초기화 이메일 발송",
            description = """
                    사용자가 입력한 이메일 주소로 비밀번호 재설정 링크를 발송합니다.
                    
                    - 해당 이메일에 등록된 사용자가 존재하면, 일회용 비밀번호 초기화 토큰을 생성하여 이메일로 전송합니다.
                    - 이메일에는 비밀번호 재설정 페이지로 이동할 수 있는 URL이 포함됩니다.
                    - 보안을 위해, 등록되지 않은 이메일이라도 성공 응답을 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공")
    })
    @PostMapping("/password-reset/request")
    public ResponseEntity<BaseResponse<String>> requestPasswordReset(
            @RequestBody @Valid PasswordResetDto.PasswordResetRequest req) {

        passwordResetService.createPasswordResetTokenAndSendEmail(req.getEmail());

        return ResponseEntity.ok()
                .body(BaseResponse.success("send password reset email success"));
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = """
                    사용자가 이메일을 통해 받은 토큰을 이용하여 비밀번호를 재설정합니다.
                    
                    - 유효한 토큰이 확인되면, 사용자의 비밀번호를 새로운 비밀번호로 변경합니다.
                    - 토큰은 일회용이며, 만료되었거나 잘못된 경우에는 실패 응답을 반환합니다.
                    - 비밀번호는 별도의 복잡도 요구 사항을 만족해야 합니다..
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰입니다.")
    })
    @PostMapping("/password-reset")
    public ResponseEntity<BaseResponse<String>> confirmPasswordReset(
            @RequestBody @Valid PasswordResetDto.PasswordResetSubmission req
    ) {
        passwordResetService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(BaseResponse.success("password reset success"));
    }
}
