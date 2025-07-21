package com.example.backend.auth.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PasswordResetDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "비밀번호 변경 요청 dto")
    public static class PasswordResetRequest {

        @Email
        @NotBlank
        @Schema(
                description = "User의 이메일 정보",
                example = "hong@gmail.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "비밀번호 변경 dto")
    public static class PasswordResetSubmission {

        @NotBlank
        @Schema(
                description = "고유 토큰 값(UUID)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String token;

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\"\\\\|,.<>/?]).{10,}$",
                message = "비밀번호는 10자 이상이며, 최소 하나의 대문자와 하나 이상의 특수문자를 포함해야 합니다."
        )
        @Schema(
                description = "새로운 비밀번호",
                example = "Password1!",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String newPassword;
    }
}
