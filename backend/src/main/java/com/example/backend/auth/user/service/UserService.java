package com.example.backend.auth.user.service;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.UserRequestDto.SignupDto;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final EmailService emailService;

    /**
     * Local 유저 회원가입
     *
     * @param req
     */
    @Transactional
    public void registerUser(SignupDto req) {
        log.info("[Register] 로컬 회원가입 시도: email={}", req.getEmail());

        if (userRepository.existsByEmail(req.getEmail())) {
            log.warn("[Register] 이메일 중복으로 회원가입 실패: email={}", req.getEmail());
            throw new CustomException(ErrorCode.USER_EMAIL_DUPLICATED);
        }

        Users user = Users.builder()
                .email(req.getEmail())
                .name(req.getName())
                .password(passwordEncoder.encode(req.getPassword()))
                .phoneNumber(req.getPhoneNumber())
                .status(Users.UserStatus.UNVERIFIED)
                .provider("local")
                .roles("USER")
                .lastLogin(null)
                .createdAt(LocalDateTime.now())
                .deletedAt(null)
                .build();

        userRepository.save(user);
        log.info("[Register] 로컬 회원가입 성공: email={}", req.getEmail());

        emailService.sendVerificationEmailAsync(user);
        log.info("[Register] 이메일 인증 메일 발송 요청 완료: email={}", user.getEmail());
    }


    /**
     * OAuth2 유저 회원가입
     *
     * @param registrationId
     * @param oauth2User
     */
    @Transactional
    public void registerUser(String registrationId, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();

        log.info("[Register-OAuth2] {} 회원가입 시도: email={}", registrationId, email);

        if (userRepository.existsByEmail(email)) {
            log.info("[Register-OAuth2] 기존 사용자 로그인 처리: email={}", email);
            userRepository.findByEmail(email).get();
            return;
        }

        String randomPwd = UUID.randomUUID().toString();
        Users user = Users.builder()
                .email(email)
                .name(name)
                .phoneNumber(null)
                .password(randomPwd)
                .provider(registrationId)
                .status(Users.UserStatus.ACTIVE)
                .lastLogin(null)
                .createdAt(LocalDateTime.now())
                .deletedAt(null)
                .roles("USER")
                .build();

        userRepository.save(user);
        log.info("[Register-OAuth2] {} 회원가입 성공: email={}", registrationId, email);

    }

    /**
     * 마지막 로그인 시간 갱신
     *
     * @param user
     */
    public void setLastLogin(Users user) {

        Users.UserStatus userStatus = user.getStatus();

        if (userStatus.equals(Users.UserStatus.DORMANT)) {
            throw new CustomException(ErrorCode.USER_DORMANT);
        } else if (userStatus.equals(Users.UserStatus.UNVERIFIED)) {
            throw new CustomException(ErrorCode.USER_UNVERIFIED);
        } else if (userStatus.equals(Users.UserStatus.WITHDRAWN)) {
            throw new CustomException(ErrorCode.USER_WITHDRAWN);
        }

        user.setLastLogin(LocalDateTime.now());

        Users.UserStatus status = user.getStatus();

        // 유저의 상태가 ACTIVE가 아닐 경우 예외처리
        if (status == Users.UserStatus.DORMANT) {
            throw new CustomException(ErrorCode.USER_DORMANT);
        } else if (status == Users.UserStatus.WITHDRAWN) {
            throw new CustomException(ErrorCode.USER_WITHDRAWN);
        } else if (status == Users.UserStatus.UNVERIFIED) {
            throw new CustomException(ErrorCode.USER_UNVERIFIED);
        }

        userRepository.save(user);
    }

    /**
     * 회원 탈퇴 처리
     *
     * @Param Users
     */
    @Transactional
    public void withdrawCurrentUser(Users user) {

        user.setStatus(Users.UserStatus.WITHDRAWN);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

    }

    public void setUserStatusActive(Users user) {
        user.setStatus(Users.UserStatus.ACTIVE);
        userRepository.save(user);
    }
}