package com.example.backend.auth.email.service;

import com.example.backend.auth.email.model.VerificationToken;
import com.example.backend.auth.email.repository.VerificationTokenRepository;
import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendVerificationEmail(Users user, String token) {

        log.info("[EmailService] 이메일 인증 메일 발송 시작: email={}, token={}", user.getEmail(), token);
        String link = frontendUrl + "/api/auth/verify-email?token=" + token;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("[Pipely] 이메일 인증 안내");
        mail.setText(
                "안녕하세요!\n\n" +
                        "다음 링크를 클릭하여 이메일 인증을 완료하세요:\n" +
                        link + "\n\n" +
                        "감사합니다."
        );

        mailSender.send(mail);
        log.info("[EmailService] 이메일 인증 메일 발송 완료: email={}", user.getEmail());
    }

    @Async
    public void sendVerificationEmailAsync(Users user) {

        log.info("[EmailService] 비동기 이메일 발송 요청 수신: email={}", user.getEmail());
        VerificationToken vt = createToken(user);
        sendVerificationEmail(user, vt.getToken());
    }

    /**
     * 비밀번호 재설정 이메일 비동기 발송
     *
     * @param user 대상 사용자
     */
    @Async
    public void sendPasswordResetEmailAsync(Users user, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[Pipely] 비밀번호 재설정 안내");

        String text = new StringBuilder()
                .append(user.getName()).append("님,\n\n")
                .append("비밀번호 재설정을 요청하셨습니다. 아래 링크를 클릭하여 새로운 비밀번호를 설정해주세요.\n\n")
                .append(link).append("\n\n")
                .append("※ 해당 링크는 요청일로부터 1시간 동안만 유효합니다.\n")
                .append("만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시해주세요.\n\n")
                .append("감사합니다.")
                .toString();
        message.setText(text);
        mailSender.send(message);
        log.info("[EmailService] 비밀번호 재설정 이메일 발송: to={}", user.getEmail());
    }

    /**
     * 이메일 인증 토큰 생성
     */
    public VerificationToken createToken(Users user) {
        String token = UUID.randomUUID().toString();

        log.info("[TokenService] 인증 토큰 생성: email={}, token={}",
                user.getEmail(), token);

        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1L))
                .build();

        return verificationTokenRepository.save(vt);
    }

    /**
     * 이메일 인증 토큰 검증
     */
    public Users validateToken(String token) {
        log.info("[TokenService] 인증 토큰 검증 요청: token={}", token);

        Optional<VerificationToken> vtOpt = verificationTokenRepository.findById(token);

        if (vtOpt.isEmpty()) {
            log.warn("[TokenService] 유효하지 않은 토큰: token={}", token);
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
        }

        VerificationToken vt = vtOpt.get();
        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("[TokenService] 만료된 토큰: token={}, 만료일시={}", token, vt.getExpiryDate());
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
        }

        log.info("[TokenService] 토큰 유효: token={}, 사용자={}", token, vt.getUser().getEmail());
        return vt.getUser();
    }
}