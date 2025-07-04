package com.example.backend.auth.email.controller;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.service.UserService;
import com.example.backend.exception.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Email API", description = "회원가입 후 이메일 인증을 처리하는 API입니다.")
@Validated
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final UserService userService;
    private final EmailService emailService;

    @Operation(
            summary = "이메일 인증 처리",
            description = """
                    회원가입 시 사용자에게 발송된 인증 이메일에 포함된 토큰을 이용해 계정을 활성화합니다.
                    
                    - 사용자는 이메일 링크를 클릭하여 이 API를 호출합니다.
                    - 전달된 토큰을 검증하여 사용자가 실제로 이메일을 소유하고 있는지 확인합니다.
                    - 검증 성공 시 사용자 계정 상태는 `ACTIVE`로 변경됩니다.
                    - 실패 시 400 Bad Request 응답을 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공, 계정이 활성화됨"),
            @ApiResponse(responseCode = "400", description = "유효하지 않거나 만료된 토큰")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<BaseResponse<String>> verifyEmail(
            @Parameter(
                    description = "이메일 인증 토큰 (UUID 형식)",
                    required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @RequestParam @NotNull UUID token
    ) {

        // Token 검증
        Users user = emailService.validateToken(token);

        // User 상태를 ACTIVE로 바꿈
        userService.setUserStatusActive(user);

        return ResponseEntity.ok()
                .body(BaseResponse.success("email verified"));
    }
}
