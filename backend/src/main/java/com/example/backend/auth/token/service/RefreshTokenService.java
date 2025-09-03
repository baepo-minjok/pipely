package com.example.backend.auth.token.service;

import com.example.backend.auth.token.model.RefreshToken;
import com.example.backend.auth.token.repository.RefreshTokenRepository;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.service.CustomUserDetails;
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
     * Generate and store a new refresh token.
     * - If a token is already stored for the same user, delete it or expire it (rotate method selected).
     * - Typically called upon login.
     *
     * @param authentication user authentication info
     */
    @Transactional
    public String createRefreshToken(Authentication authentication) {

        Users user;
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            user = ((CustomUserDetails) authentication.getPrincipal()).getUserEntity();
        } else {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        refreshTokenRepository.deleteByUser(user);

        String token = jwtTokenProvider.createRefreshToken(authentication);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenDurationMs)))
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    /**
     * After validating the refresh token, return Users.
     * - If the token does not exist, has expired, or is revoked=true, an exception is raised or Optional. Empty is processed.
     *
     * @param token Token to Verify
     */
    public void validateRefreshTokenAndGetUser(String token) {

        if (token == null) {
            throw new CustomException(ErrorCode.USER_REFRESH_TOKEN_INVALID);
        }
        Optional<RefreshToken> optionalRT = refreshTokenRepository.findById(token);
        if (optionalRT.isEmpty()) {
            throw new CustomException(ErrorCode.USER_REFRESH_TOKEN_INVALID);
        }
        RefreshToken refreshToken = optionalRT.get();

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException(ErrorCode.USER_REFRESH_TOKEN_EXPIRED);
        }
    }

    /**
     * Delete existing refresh token when logging out or reissuing
     *
     * @param user Delete existing refresh token when logging out or reissuing
     */
    @Transactional
    public void deleteByUser(Users user) {
        refreshTokenRepository.deleteByUser(user);
    }

}
