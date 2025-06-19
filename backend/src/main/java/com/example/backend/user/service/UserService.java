package com.example.backend.user.service;

import com.example.backend.user.model.Users;
import com.example.backend.user.model.VerificationToken;
import com.example.backend.user.model.dto.SignupRequest;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;

    /**
     * Local 회원가입 처리
     */
    @Transactional
    public void registerUser(SignupRequest req) {
        log.info("[Register] 로컬 회원가입 시도: email={}", req.getEmail());

        if (userRepository.existsById(req.getEmail())) {
            log.warn("[Register] 이메일 중복으로 회원가입 실패: email={}", req.getEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Users user = Users.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .type("local")
                .roles("USER")
                .build();

        userRepository.save(user);
        log.info("[Register] 로컬 회원가입 성공: email={}", req.getEmail());

        VerificationToken vt = verificationTokenService.createToken(user);
        log.info("[Register] 이메일 인증 토큰 생성: token={}", vt.getToken());

        emailService.sendVerificationEmailAsync(user, vt.getToken());
        log.info("[Register] 이메일 인증 메일 발송 요청 완료: email={}", user.getEmail());
    }

    /**
     * OAuth2 회원가입 처리
     */
    @Transactional
    public Users registerUser(String registrationId, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");

        log.info("[Register-OAuth2] {} 회원가입 시도: email={}", registrationId, email);

        if (userRepository.existsById(email)) {
            log.info("[Register-OAuth2] 기존 사용자 로그인 처리: email={}", email);
            return userRepository.findById(email).get();
        }

        String randomPwd = UUID.randomUUID().toString();
        Users user = Users.builder()
                .email(email)
                .password(randomPwd)
                .type(registrationId)
                .enabled(true)
                .roles("USER")
                .build();

        userRepository.save(user);
        log.info("[Register-OAuth2] {} 회원가입 성공: email={}", registrationId, email);

        return user;
    }

    /**
     * 회원 탈퇴 처리
     */
    @Transactional
    public void withdrawCurrentUser(String email) {
        log.info("[Withdraw] 회원 탈퇴 요청: email={}", email);

        Users user = userRepository.findById(email)
                .orElseThrow(() -> {
                    log.warn("[Withdraw] 사용자 조회 실패: email={}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        user.setEnabled(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("[Withdraw] 회원 탈퇴 처리 완료: email={}, 삭제일시={}", email, user.getDeletedAt());
    }

}