package com.example.backend.auth.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserRequestDto {

    /**
     * 회원가입 요청을 위한 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "SignupDto", description = "회원가입 요청 DTO")
    public static class SignupDto {

        @Schema(
                description = "사용자 이메일",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Email
        @NotBlank
        private String email;

        @Schema(
                description = "비밀번호 (최소 10자, 대문자 1개, 특수문자 1개 포함)",
                example = "Password@123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\"\\\\|,.<>/?]).{10,}$")
        private String password;

        @Schema(
                description = "사용자 이름",
                example = "홍길동",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        private String name;

        @Schema(
                description = "휴대폰 번호",
                example = "010-1234-5678",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Pattern(regexp = "01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$")
        private String phoneNumber;

    }

    /**
     * 로그인 요청을 위한 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "LoginRequest", description = "로그인 요청 DTO")
    public static class LoginRequest {

        @Schema(
                description = "사용자 이메일",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Email
        @NotBlank
        private String email;

        @Schema(
                description = "비밀번호",
                example = "Password@123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        private String password;
    }
}
