package com.example.backend.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 인증 후 반환할 토큰 정보 DTO
 */
@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}

