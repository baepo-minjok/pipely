package com.example.backend.auth.email.service;

import com.example.backend.auth.email.model.VerificationToken;
import com.example.backend.auth.email.repository.VerificationTokenRepository;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.auth.user.service.DormantTokenService;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    @Value("${user.dormancy.period.days}")
    private long dormancyPeriodDays;

    public void sendVerificationEmail(Users user, UUID token) {
        log.info("[EmailService] 이메일 인증 메일 발송 시작: email={}, token={}", user.getEmail(), token);

        String link = "https://www.pipely.kro.kr/user/email/verify?token=" + token;

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {

            helper.setTo(user.getEmail());
            helper.setSubject("[Pipely] 이메일 인증 안내");

            String html = """
                    <div style="font-family: Arial, sans-serif; line-height: 1.7;">
                        <h2 style="color:#1976d2; margin-bottom:12px;">Pipely 이메일 인증 안내</h2>
                        <p>안녕하세요, <b>%s</b>님!</p>
                        <p>아래 버튼을 클릭하여 이메일 인증을 완료해 주세요.</p>
                        <a href="%s" style="
                            display:inline-block;
                            padding:12px 28px;
                            background:#1976d2;
                            color:#fff;
                            border-radius:5px;
                            font-size:1rem;
                            font-weight:bold;
                            text-decoration:none;
                            margin:16px 0 22px 0;
                            ">이메일 인증하기</a>
                        <p style="color:#888; font-size:0.95em;">
                            ※ 본 메일은 회원 가입 또는 이메일 변경을 위해 발송되었습니다.<br>
                            ※ 본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                        </p>
                        <div style="margin-top:32px;">감사합니다.<br><b>Pipely 팀</b></div>
                    </div>
                    """.formatted(user.getName(), link);

            helper.setText(html, true); // true = HTML 메일

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

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
        String link = "https://www.pipely.kro.kr/user/reset/password?token=" + token;

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            helper.setTo(user.getEmail());
            helper.setSubject("[Pipely] 비밀번호 재설정 안내");

            String html = """
                    <div style="font-family:Arial,sans-serif; line-height:1.7;">
                        <p><b>%s</b>님,</p>
                        <p>비밀번호 재설정을 요청하셨습니다.<br>
                        아래 버튼을 클릭하여 새로운 비밀번호를 설정해주세요.</p>
                        <a href="%s" style="
                            display:inline-block;
                            padding:10px 30px;
                            background:#1976d2;
                            color:#fff;
                            border-radius:5px;
                            font-weight:bold;
                            text-decoration:none;
                            margin:20px 0 30px 0;
                            ">비밀번호 재설정</a>
                        <p>※ 해당 링크는 요청일로부터 <b>1시간</b> 동안만 유효합니다.<br>
                        만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시해주세요.</p>
                        <div style="margin-top:30px;">감사합니다.<br><b>Pipely 팀</b></div>
                    </div>
                    """.formatted(user.getName(), link);

            helper.setText(html, true); // 두번째 파라미터 true: HTML 메일

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

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
        verificationTokenRepository.deleteById(token);
        return vt.getUser();
    }

    public void sendDormantNotificationEmail(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String token = dormantTokenService.createDormantReactivationToken(user);
        String activationLink = "https://www.pipely.kro.kr/user/reactivate?token=" + token;

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("[서비스명] 계정 휴면 안내 및 재활성화 방법");

            // HTML 메일 내용
            String htmlMsg = """
                    <div style="font-family:Arial,sans-serif;">
                        <p>안녕하세요. 귀하의 계정이 %d일간 미사용되어 휴면 처리되었습니다.</p>
                        <p>다시 서비스 이용을 원하시면 아래 버튼을 눌러 계정을 활성화해주세요:</p>
                        <a href="%s" style="
                            display:inline-block;
                            padding:10px 24px;
                            background-color:#1a73e8;
                            color:#fff;
                            text-decoration:none;
                            border-radius:4px;
                            font-weight:bold;">계정 재활성화</a>
                        <p style="margin-top:30px;">감사합니다.</p>
                    </div>
                    """.formatted(dormancyPeriodDays, activationLink);

            helper.setText(htmlMsg, true); // true = HTML 메일
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}