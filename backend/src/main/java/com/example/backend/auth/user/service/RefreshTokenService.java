package com.example.backend.auth.user.service;

import com.example.backend.auth.user.model.RefreshToken;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.RefreshTokenRepository;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenDurationMs;

    /**
     * 새 Refresh Token 생성 및 저장.
     * - 기존에 동일 사용자에 대해 저장된 토큰이 있다면 삭제하거나 만료 처리(rotate 방식 선택).
     * - 보통 로그인 시 호출.
     */
    @Transactional
    public String createRefreshToken(Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();
        // 기존 토큰 삭제: 한 사용자당 하나의 활성 Refresh Token만 허용하는 경우
        refreshTokenRepository.deleteByUser(user);

        // 토큰 문자열 생성
        String token = jwtTokenProvider.createRefreshToken(authentication);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenDurationMs)))
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    /**
     * Refresh Token 유효성 검사 후, Users 반환.
     * - 토큰이 존재하지 않거나 만료됐거나 revoked=true면 예외 발생 혹은 Optional.empty 처리
     */
    public void validateRefreshTokenAndGetUser(String token) {

        if (token == null) {
            // Cookie에 refresh 토큰이 없는 경우
            throw new CustomException(ErrorCode.USER_REFRESH_TOKEN_INVALID);
        }
        Optional<RefreshToken> optionalRT = refreshTokenRepository.findByToken(token);
        if (optionalRT.isEmpty()) {
            // DB에 token이 없는 경우
            throw new CustomException(ErrorCode.USER_REFRESH_TOKEN_INVALID);
        }
        RefreshToken refreshToken = optionalRT.get();

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            // 만료된 경우
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException(ErrorCode.USER_REFRESH_TOKEN_EXPIRED);
        }
    }

    /**
     * 로그아웃 또는 재발급 시 기존 Refresh Token 삭제
     */
    public void deleteByUser(Users user) {
        refreshTokenRepository.deleteByUser(user);
    }

}
