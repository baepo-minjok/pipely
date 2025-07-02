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
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Email API", description = "이메일 인증 관련 API")
@Validated
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final UserService userService;
    private final EmailService emailService;

    @Operation(summary = "이메일 인증", description = "이메일로 발급한 토큰으로 사용자 인증")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<BaseResponse<String>> verifyEmail(
            @Parameter(description = "이메일 인증 토큰")
            @RequestParam @NotBlank(message = "토큰은 필수입니다.") UUID token
    ) {

        // Token 검증
        Users user = emailService.validateToken(token);

        // User 상태를 ACTIVE로 바꿈
        userService.setUserStatusActive(user);

        return ResponseEntity.ok()
                .body(BaseResponse.success("email verified"));
    }
}
