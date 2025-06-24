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
     * 휴면 재활성화를 위한 토큰 생성.
     * - 기존에 남아있는 토큰들은 삭제
     * - 새 토큰 발급, 저장하여 반환
     *
     * @param user 휴면 대상 사용자 엔티티 (Users)
     * @return 생성된 토큰 문자열
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
     * 토큰을 검증하고, 유효하면 관련된 Users 엔티티를 반환.
     * - 토큰이 없거나 만료되었거나 이미 사용되었으면 예외 발생
     *
     * @param tokenStr 클라이언트로부터 전달된 토큰 문자열
     * @return 토큰이 유효한 경우 해당 사용자 엔티티
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
     * 재활성화 완료 후 토큰 삭제.
     *
     * @param tokenStr 사용이 끝난 토큰 문자열
     */
    @Transactional
    public void deleteToken(String tokenStr) {
        tokenRepository.findByToken(tokenStr).ifPresent(tokenRepository::delete);
    }

    /**
     * 사용자 기준으로 토큰 모두 삭제 (안전용).
     *
     * @param user
     */
    @Transactional
    public void deleteTokensByUser(Users user) {
        tokenRepository.deleteByUser(user);
    }
}
