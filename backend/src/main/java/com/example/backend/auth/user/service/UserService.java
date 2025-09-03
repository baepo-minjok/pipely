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
     * Register a new local user (email/password). Sends verification email.
     *
     * @param req DTO containing local signup request data
     */
    @Transactional
    public void registerUser(SignupDto req) {

        if (userRepository.existsByEmail(req.getEmail())) {
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

        emailService.sendVerificationEmailAsync(user);
    }

    /**
     * Register a new user via OAuth2 (with claims from JWT cookie).
     *
     * @param oAuthCookie oAuth2Cookie issued when logging in with oAuth2
     * @param dto         DTO containing OAuth2 signup request data
     */
    public void oAuth2UserSignUp(String oAuthCookie, OAuth2SignupDto dto) {

        if (!jwtTokenProvider.validateToken(oAuthCookie)) {
            throw new CustomException(ErrorCode.USER_OAUTH2_TOKEN_INVALID);
        }
        String registrationId = jwtTokenProvider.getRegistrationId(oAuthCookie);
        Map<String, Object> map = jwtTokenProvider.getClaims(oAuthCookie);
        String email = (String) map.get("email");

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
    }


    /**
     * Check if an OAuth2 user already exists. If yes, update lastLogin; else return false.
     *
     * @param oauth2User Logged in oauth2 user
     */
    @Transactional
    public boolean isExist(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = attributes.get("email").toString();

        if (userRepository.existsByEmail(email)) {
            Users user = findByEmail(email);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update user's last login timestamp. Throws exceptions if user is DORMANT, UNVERIFIED, or WITHDRAWN.
     *
     * @param user logged in user
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

        userRepository.save(user);
    }

    /**
     * Mark user as WITHDRAWN and set deletedAt timestamp.
     *
     * @param user User who request
     */
    @Transactional
    public void withdrawCurrentUser(Users user) {

        user.setStatus(Users.UserStatus.WITHDRAWN);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

    }

    /**
     * Change user status to ACTIVE and save.
     *
     * @param user User who request
     */
    public void setUserStatusActive(Users user) {
        user.setStatus(Users.UserStatus.ACTIVE);
        userRepository.save(user);
    }

    /**
     * Check if email already exists; throws exception if duplicated.
     *
     * @param email Email to check for duplicates
     */
    public void checkDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.USER_EMAIL_DUPLICATED);
        }
    }

    /**
     * Find user by email or throw USER_NOT_FOUND.
     *
     * @param email Email who request
     * @return User using this email
     */
    public Users findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Retrieve user details along with linked Jenkins info list.
     *
     * @param email Email who request
     * @return DTO containing detailed user information
     */
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

    /**
     * Reactivate a withdrawn user (set ACTIVE and clear deletedAt).
     *
     * @param email Email who request
     */
    @Transactional
    public void reactivation(String email) {
        Users user = findByEmail(email);

        user.setStatus(Users.UserStatus.ACTIVE);
        user.setDeletedAt(null);

        userRepository.save(user);
    }
}