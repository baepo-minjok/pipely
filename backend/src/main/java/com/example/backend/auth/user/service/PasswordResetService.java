package com.example.backend.auth.user.service;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.PasswordResetToken;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.PasswordResetTokenRepository;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService; // 이메일 발송 서비스
    private final PasswordEncoder passwordEncoder;

    // 토큰 만료 기간: 예시 1시간
    private final long EXPIRATION_HOURS = 1L;

    /**
     * 비밀번호 재설정 요청 처리: 토큰 생성 및 이메일 발송
     *
     * @param email 요청한 이메일
     */
    @Transactional
    public void createPasswordResetTokenAndSendEmail(String email) {
        // 사용자 존재 여부 조회
        Optional<Users> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // 이메일 존재하지 않더라도, 응답 메시지는 동일하게 처리함 (보안상 존재 여부 노출 방지)
            log.warn("[Password Reset] 존재하지 않는 이메일로 요청: {}", email);
            return;
        }
        Users user = userOpt.get();

        // 기존 토큰 정리
        tokenRepository.deleteAllByUser(user);

        // 새로운 토큰 생성
        String tokenStr = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken prt = PasswordResetToken.builder()
                .token(tokenStr)
                .user(user)
                .createdAt(now)
                .expiresAt(now.plusHours(EXPIRATION_HOURS))
                .build();
        tokenRepository.save(prt);
        log.info("[Password Reset] 토큰 생성: userEmail={}, token={}", email, tokenStr);

        // 이메일 전송
        emailService.sendPasswordResetEmailAsync(user, tokenStr);
        log.info("[Password Reset] 이메일 전송 요청 완료: to={}", email);
    }

    /**
     * 비밀번호 변경 처리: 토큰 검증 후 새로운 비밀번호 저장
     *
     * @param tokenStr    클라이언트가 제공한 토큰
     * @param newPassword 평문 새 비밀번호
     */
    @Transactional
    public void resetPassword(String tokenStr, String newPassword) {

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isEmpty()) {
            log.warn("[Password Reset] resetPassword: 토큰 존재하지 않음: {}", tokenStr);
            throw new CustomException(ErrorCode.USER_PASSWORD_RESET_TOKEN_INVALID);
        }
        PasswordResetToken prt = tokenOpt.get();

        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("[Password Reset] resetPassword: 유효하지 않은 토큰: {}", tokenStr);
            throw new CustomException(ErrorCode.USER_PASSWORD_RESET_TOKEN_INVALID);
        }
        Users user = prt.getUser();

        // 비밀번호 인코딩 후 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 사용된 토큰은 삭제
        tokenRepository.deleteAllByUser(user);

        log.info("[Password Reset] 비밀번호 변경 완료: userEmail={}", user.getEmail());
    }
}
