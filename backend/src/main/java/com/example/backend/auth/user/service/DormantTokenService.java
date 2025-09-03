package com.example.backend.auth.user.service;

import com.example.backend.auth.user.model.DormantActivationToken;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.DormantActivationTokenRepository;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DormantTokenService {

    private final DormantActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${user.dormancy.token.expiration.hours:24}")
    private long tokenExpirationHours;

    /**
     * Generate a token for dormant account reactivation.
     * - Deletes all existing tokens for the user (safety cleanup).
     * - Creates a new UUID-based token and stores it in the DB.
     * - Expiration time is calculated using {@code tokenExpirationHours}.
     *
     * @param user Dormant user entity (must exist in DB)
     * @return Generated token string
     */
    @Transactional
    public String createDormantReactivationToken(Users user) {
        // 사용자가 null이거나 DB에 없으면 예외
        if (user == null || user.getId() == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 기존 토큰 삭제 (여러 개 있을 수 있으므로 모두 삭제)
        tokenRepository.deleteByUser(user);

        // 토큰 문자열 생성: UUID 등
        String token = UUID.randomUUID().toString();

        // 만료 시각 계산: 현재 시각 + 만료 시간
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(tokenExpirationHours);

        DormantActivationToken entity = DormantActivationToken.builder()
                .token(token)
                .user(user)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(entity);
        return token;
    }

    /**
     * Validate a dormant reactivation token and return the associated user.
     * - If token is not found, expired, or invalid → throw exception.
     * - If valid, mark user status as ACTIVE and update lastLogin timestamp.
     *
     * @param tokenStr Token string from the client
     * @return User entity linked to the valid token
     */
    @Transactional(readOnly = true)
    public Users validateAndGetUserByToken(String tokenStr) {
        DormantActivationToken tokenEntity = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_DORMANT_TOKEN_INVALID));

        // 만료 여부 확인
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 만료된 토큰: 삭제 후 예외
            tokenRepository.delete(tokenEntity);
            throw new CustomException(ErrorCode.USER_DORMANT_TOKEN_EXPIRED);
        }

        Users user = tokenEntity.getUser();
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        user.setStatus(Users.UserStatus.ACTIVE);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    /**
     * Delete all tokens for a specific user (safety cleanup).
     *
     * @param user User entity whose tokens should be deleted
     */
    @Transactional
    public void deleteTokensByUser(Users user) {
        tokenRepository.deleteByUser(user);
    }
}
