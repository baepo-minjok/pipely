package com.example.backend.auth.user.model.dto;

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
    public static class PasswordResetRequest {
        @Email
        @NotBlank
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordResetTokenValidationRequest {
        @NotBlank
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordResetSubmission {
        @NotBlank
        private String token;

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\"\\\\|,.<>/?]).{10,}$",
                message = "비밀번호는 10자 이상이며, 최소 하나의 대문자와 하나 이상의 특수문자를 포함해야 합니다."
        )
        private String newPassword;
    }
}
