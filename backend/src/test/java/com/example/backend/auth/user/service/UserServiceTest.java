package com.example.backend.auth.user.service;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.UserRequestDto.SignupDto;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private SignupDto signupDto;
    private Users sampleUser;

    @BeforeEach
    void setUp() {
        signupDto = SignupDto.builder()
                .email("test@example.com")
                .name("Test User")
                .password("password123")
                .phoneNumber("010-1234-5678")
                .build();
        sampleUser = Users.builder()
                .email(signupDto.getEmail())
                .name(signupDto.getName())
                .password("encodedPwd")
                .phoneNumber(signupDto.getPhoneNumber())
                .status(Users.UserStatus.UNVERIFIED)
                .provider("local")
                .roles("USER")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("로컬 회원가입 성공 시 repository.save와 이메일 전송 호출")
    void registerUser_Success() {
        when(userRepository.existsByEmail(signupDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupDto.getPassword())).thenReturn("encodedPwd");

        userService.registerUser(signupDto);

        verify(userRepository).save(any(Users.class));
        verify(emailService).sendVerificationEmailAsync(any(Users.class));
    }

    @Test
    @DisplayName("로컬 회원가입 이메일 중복 시 예외 발생")
    void registerUser_DuplicateEmail() {
        when(userRepository.existsByEmail(signupDto.getEmail())).thenReturn(true);

        CustomException ex = assertThrows(CustomException.class, () -> userService.registerUser(signupDto));
        assertEquals(ErrorCode.USER_EMAIL_DUPLICATED, ex.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmailAsync(any());
    }

    @Test
    @DisplayName("OAuth2 회원가입: 신규 사용자인 경우 save 호출")
    void registerUser_OAuth2_NewUser() {
        String registrationId = "google";
        OAuth2User oauth2User = mock(OAuth2User.class);
        Map<String, Object> attrs = Map.of("email", "oauth@example.com", "name", "OAuth User");
        when(oauth2User.getAttributes()).thenReturn(attrs);
        when(userRepository.existsByEmail("oauth@example.com")).thenReturn(false);

        userService.registerUser(registrationId, oauth2User);

        verify(userRepository).save(any(Users.class));
    }

    @Test
    @DisplayName("OAuth2 회원가입: 기존 사용자인 경우 save 미호출")
    void registerUser_OAuth2_ExistingUser() {
        String registrationId = "google";
        OAuth2User oauth2User = mock(OAuth2User.class);
        Map<String, Object> attrs = Map.of("email", "oauth@example.com", "name", "OAuth User");
        when(oauth2User.getAttributes()).thenReturn(attrs);
        when(userRepository.existsByEmail("oauth@example.com")).thenReturn(true);
        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(sampleUser));

        userService.registerUser(registrationId, oauth2User);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("setLastLogin: 비활성 상태 유저는 예외 발생")
    void setLastLogin_InvalidStatus() {
        sampleUser.setStatus(Users.UserStatus.DORMANT);
        CustomException ex = assertThrows(CustomException.class, () -> userService.setLastLogin(sampleUser));
        assertEquals(ErrorCode.USER_DORMANT, ex.getErrorCode());
    }

    @Test
    @DisplayName("setLastLogin: 활성 상태 유저의 마지막 로그인 업데이트")
    void setLastLogin_Success() {
        sampleUser.setStatus(Users.UserStatus.ACTIVE);

        userService.setLastLogin(sampleUser);

        assertNotNull(sampleUser.getLastLogin());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("withdrawCurrentUser: 상태 변경 및 deletedAt 설정 후 save 호출")
    void withdrawCurrentUser() {
        sampleUser.setStatus(Users.UserStatus.ACTIVE);

        userService.withdrawCurrentUser(sampleUser);

        assertEquals(Users.UserStatus.WITHDRAWN, sampleUser.getStatus());
        assertNotNull(sampleUser.getDeletedAt());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("setUserStatusActive: 상태를 ACTIVE로 변경 후 save 호출")
    void setUserStatusActive() {
        sampleUser.setStatus(Users.UserStatus.UNVERIFIED);

        userService.setUserStatusActive(sampleUser);

        assertEquals(Users.UserStatus.ACTIVE, sampleUser.getStatus());
        verify(userRepository).save(sampleUser);
    }
}
