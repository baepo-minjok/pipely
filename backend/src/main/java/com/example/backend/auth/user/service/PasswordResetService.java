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

    private final long EXPIRATION_HOURS = 1L;

    /**
     * Processing password reset requests: Generating tokens and sending emails
     *
     * @param email Email address requesting issuance
     */
    @Transactional
    public void createPasswordResetTokenAndSendEmail(String email) {
        Optional<Users> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }
        Users user = userOpt.get();

        String tokenStr = createPasswordResetToken(user);

        emailService.sendPasswordResetEmailAsync(user, tokenStr);
    }

    /**
     * Generate a new password reset token
     *
     * @param user User who request
     * @return new password token
     */
    @Transactional
    public String createPasswordResetToken(Users user) {
        tokenRepository.deleteAllByUser(user);

        String tokenStr = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken prt = PasswordResetToken.builder()
                .token(tokenStr)
                .user(user)
                .createdAt(now)
                .expiresAt(now.plusHours(EXPIRATION_HOURS))
                .build();
        tokenRepository.save(prt);
        return tokenStr;
    }

    /**
     * Generate a new password reset token
     *
     * @param email email who request
     * @return new password token
     */
    @Transactional
    public String getToken(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return createPasswordResetToken(user);
    }

    /**
     * Password Change Processing: Store the new password after token verification
     *
     * @param tokenStr    The client-supplied token
     * @param newPassword newPassword The plaintext new password
     */
    @Transactional
    public void resetPassword(String tokenStr, String newPassword) {

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isEmpty()) {
            throw new CustomException(ErrorCode.USER_PASSWORD_RESET_TOKEN_INVALID);
        }
        PasswordResetToken prt = tokenOpt.get();

        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.USER_PASSWORD_RESET_TOKEN_INVALID);
        }
        Users user = prt.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.deleteAllByUser(user);
    }
}
