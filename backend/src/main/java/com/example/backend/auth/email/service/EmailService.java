package com.example.backend.auth.email.service;

import com.example.backend.auth.email.model.VerificationToken;
import com.example.backend.auth.email.repository.VerificationTokenRepository;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.auth.user.service.DormantTokenService;
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
    private final DormantTokenService dormantTokenService;
    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${user.dormancy.period.days}")
    private long dormancyPeriodDays;

    public void sendVerificationEmail(Users user, UUID token) {

        log.info("[EmailService] 이메일 인증 메일 발송 시작: email={}, token={}", user.getEmail(), token);
        String link = frontendUrl + "/api/auth/email/verify-email?token=" + token;

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
        String link = frontendUrl + "/password-reset?token=" + token;
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

        VerificationToken vt = VerificationToken.builder()
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1L))
                .build();

        return verificationTokenRepository.save(vt);
    }

    /**
     * 이메일 인증 토큰 검증
     */
    public Users validateToken(UUID token) {
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

    public void sendDormantNotificationEmail(String email) {

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이메일에 포함할 재활성화 링크 토큰 생성 및 저장:
        String token = dormantTokenService.createDormantReactivationToken(user);
        String activationLink = "https://pipely.com/reactivate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[서비스명] 계정 휴면 안내 및 재활성화 방법");
        message.setText("안녕하세요. 귀하의 계정이 " + dormancyPeriodDays + "일간 미사용되어 휴면 처리되었습니다.\n"
                + "다시 서비스 이용을 원하시면 아래 링크를 통해 계정을 활성화해주세요:\n"
                + activationLink + "\n\n감사합니다.");
        mailSender.send(message);
    }
}