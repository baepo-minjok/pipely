package com.example.backend.auth.email.service;

import com.example.backend.auth.email.model.VerificationToken;
import com.example.backend.auth.email.repository.VerificationTokenRepository;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.auth.user.service.DormantTokenService;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private DormantTokenService dormantTokenService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailService emailService;

    private Users testUser;
    private UUID testToken;

    @BeforeEach
    void setUp() {
        testUser = Users.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .build();
        testToken = UUID.randomUUID();

        ReflectionTestUtils.setField(emailService, "dormancyPeriodDays", 30L);
    }

    @Test
    @DisplayName("이메일 인증 토큰 생성 테스트")
    void createToken_success() {

        VerificationToken expectedToken = VerificationToken.builder()
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(1L))
                .build();
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(expectedToken);

        VerificationToken createdToken = emailService.createToken(testUser);

        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
        assertThat(createdToken.getUser()).isEqualTo(testUser);
        assertThat(createdToken.getExpiryDate()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_validToken_returnsUser() {

        VerificationToken verificationToken = VerificationToken.builder()
                .token(testToken)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(1L))
                .build();

        when(verificationTokenRepository.findById(testToken)).thenReturn(Optional.of(verificationToken));

        Users foundUser = emailService.validateToken(testToken);

        assertThat(foundUser).isEqualTo(testUser);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패 (토큰 없음)")
    void validateToken_invalidToken_throwsException() {
        when(verificationTokenRepository.findById(testToken)).thenReturn(Optional.empty());

        CustomException thrown = assertThrows(CustomException.class, () ->
                emailService.validateToken(testToken)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_expiredToken_throwsException() {

        VerificationToken verificationToken = VerificationToken.builder()
                .token(testToken)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusDays(1L)) // 만료된 토큰
                .build();

        when(verificationTokenRepository.findById(testToken)).thenReturn(Optional.of(verificationToken));

        CustomException thrown = assertThrows(CustomException.class, () ->
                emailService.validateToken(testToken)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
    }

    @Test
    @DisplayName("휴면 알림 이메일 발송 실패 - 사용자 없음")
    void sendDormantNotificationEmail_userNotFound_throwsException() {

        String userEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        CustomException thrown = assertThrows(CustomException.class, () ->
                emailService.sendDormantNotificationEmail(userEmail)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(dormantTokenService, never()).createDormantReactivationToken(any(Users.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}