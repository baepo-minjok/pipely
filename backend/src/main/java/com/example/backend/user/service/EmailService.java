package com.example.backend.user.service;

import com.example.backend.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${app.frontend.url}") private String frontendUrl;

    public void sendVerificationEmail(Users user, String token) {

        log.info("[EmailService] 이메일 인증 메일 발송 시작: email={}, token={}", user.getEmail(), token);
        String link = frontendUrl + "/api/auth/verify-email?token=" + token;

        log.debug("[EmailService] 이메일 인증 링크 생성됨: {}", link);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("이메일 인증 안내");
        mail.setText(
                "안녕하세요!\n\n" +
                        "다음 링크를 클릭하여 이메일 인증을 완료하세요:\n" +
                        link + "\n\n" +
                        "감사합니다."
        );

        try {
            mailSender.send(mail);
            log.info("[EmailService] 이메일 인증 메일 발송 완료: email={}", user.getEmail());
        } catch (Exception e) {
            log.error("[EmailService] 이메일 전송 실패: email={}, error={}", user.getEmail(), e.getMessage(), e);
        }
    }
    @Async
    public void sendVerificationEmailAsync(Users user, String token) {
        log.info("[EmailService] 비동기 이메일 발송 요청 수신: email={}", user.getEmail());
        sendVerificationEmail(user, token);
    }
}