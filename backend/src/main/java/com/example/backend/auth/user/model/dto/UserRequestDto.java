package com.example.backend.auth.user.model.dto;

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
    public static class SignupDto {

        @Email
        @NotBlank
        private String email;

        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\"\\\\|,.<>/?]).{10,}$")
        private String password;

        @NotBlank
        private String name;

        @Pattern(regexp = "01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$")
        private String phoneNumber;

    }

    /**
     * 로그인 요청을 위한 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }
}
