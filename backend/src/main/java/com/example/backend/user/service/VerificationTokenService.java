package com.example.backend.user.service;


import com.example.backend.user.model.Users;
import com.example.backend.user.model.VerificationToken;
import com.example.backend.user.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    /**
     * 이메일 인증 토큰 생성
     */
    public VerificationToken createToken(Users user) {
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(1, ChronoUnit.DAYS);

        log.info("[TokenService] 인증 토큰 생성: email={}, token={}, 만료일시={}",
                user.getEmail(), token, expiry);

        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiry)
                .build();

        return verificationTokenRepository.save(vt);
    }

    /**
     * 이메일 인증 토큰 검증
     */
    public Optional<Users> validateToken(String token) {
        log.info("[TokenService] 인증 토큰 검증 요청: token={}", token);

        Optional<VerificationToken> vtOpt = verificationTokenRepository.findById(token);

        if (vtOpt.isEmpty()) {
            log.warn("[TokenService] 유효하지 않은 토큰: token={}", token);
            return Optional.empty();
        }

        VerificationToken vt = vtOpt.get();
        if (vt.getExpiryDate().isBefore(Instant.now())) {
            log.warn("[TokenService] 만료된 토큰: token={}, 만료일시={}", token, vt.getExpiryDate());
            return Optional.empty();
        }

        log.info("[TokenService] 토큰 유효: token={}, 사용자={}", token, vt.getUser().getEmail());
        return Optional.of(vt.getUser());
    }
}
