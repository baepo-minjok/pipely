package com.example.backend.auth.user.service;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.ResponseDto;
import com.example.backend.auth.user.model.dto.UserRequestDto.OAuth2SignupDto;
import com.example.backend.auth.user.model.dto.UserRequestDto.SignupDto;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

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

    public void oAuth2UserSignUp(String oAuthCookie, OAuth2SignupDto dto) {

        if (!jwtTokenProvider.validateToken(oAuthCookie)) {
            // 예외
            throw new CustomException(ErrorCode.USER_OAUTH2_TOKEN_INVALID);
        }
        String registrationId = jwtTokenProvider.getRegistrationId(oAuthCookie);
        Map<String, Object> map = jwtTokenProvider.getClaims(oAuthCookie);
        String email = (String) map.get("email");

        log.info("[Register-OAuth2] {} 회원가입 시도: email={}", registrationId, email);

        Users user = Users.builder()
                .email(email)
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .provider("oAuth")
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
     * OAuth2 유저 회원가입
     *
     * @param oauth2User
     */
    @Transactional
    public boolean isExist(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = attributes.get("email").toString();

        if (userRepository.existsByEmail(email)) {
            log.info("[Register-OAuth2] 기존 사용자 로그인 처리: email={}", email);
            Users user = findByEmail(email);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
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

    public void checkDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.USER_EMAIL_DUPLICATED);
        }
    }

    public Users findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public ResponseDto.detailDto findDetail(String email) {
        Users user = findByEmail(email);
        boolean isVerified = user.getStatus().equals(Users.UserStatus.ACTIVE);
        List<InfoResponseDto.LightInfoDto> infoDtoList = user.getJenkinsInfoList().stream()
                .map(InfoResponseDto.LightInfoDto::fromEntity).toList();

        return ResponseDto.detailDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .lastLogin(user.getLastLogin())
                .isVerified(isVerified)
                .infoDtoList(infoDtoList)
                .build();
    }
}