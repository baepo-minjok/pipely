package com.example.backend.auth.token.service;

import com.example.backend.auth.token.model.RefreshToken;
import com.example.backend.auth.token.repository.RefreshTokenRepository;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;
    @Mock
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private Users testUser;
    private String generatedToken;
    private long durationMs = Duration.ofDays(7).toMillis();

    @BeforeEach
    void setUp() {
        testUser = Users.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .build();
        generatedToken = "test-refresh-token";

        // Inject the @Value field
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", durationMs);
    }

    @Test
    @DisplayName("createRefreshToken - 성공: 기존 토큰 삭제 후 새 토큰 생성 및 저장")
    void createRefreshToken_success() {
        // given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserEntity()).thenReturn(testUser);
        when(jwtTokenProvider.createRefreshToken(authentication)).thenReturn(generatedToken);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        String returned = refreshTokenService.createRefreshToken(authentication);

        // then
        assertThat(returned).isEqualTo(generatedToken);
        verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
        verify(jwtTokenProvider, times(1)).createRefreshToken(authentication);
        verify(refreshTokenRepository, times(1)).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getToken()).isEqualTo(generatedToken);
        assertThat(saved.getExpiryDate()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("createRefreshToken - 실패: principal이 CustomUserDetails가 아닌 경우")
    void createRefreshToken_invalidPrincipal_throwsException() {
        when(authentication.getPrincipal()).thenReturn(new Object());

        CustomException ex = assertThrows(CustomException.class, () ->
                refreshTokenService.createRefreshToken(authentication)
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(refreshTokenRepository, never()).deleteByUser(any());
        verify(jwtTokenProvider, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("validateRefreshTokenAndGetUser - 성공: 유효한 토큰")
    void validateRefreshToken_validToken_noException() {
        RefreshToken token = RefreshToken.builder()
                .token(generatedToken)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
        when(refreshTokenRepository.findById(generatedToken)).thenReturn(Optional.of(token));

        // should not throw
        refreshTokenService.validateRefreshTokenAndGetUser(generatedToken);

        verify(refreshTokenRepository, never()).delete(token);
    }

    @Test
    @DisplayName("validateRefreshTokenAndGetUser - 실패: 토큰이 null인 경우")
    void validateRefreshToken_nullToken_throwsException() {
        CustomException ex = assertThrows(CustomException.class, () ->
                refreshTokenService.validateRefreshTokenAndGetUser(null)
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("validateRefreshTokenAndGetUser - 실패: DB에 토큰이 없는 경우")
    void validateRefreshToken_notFound_throwsException() {
        when(refreshTokenRepository.findById(generatedToken)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () ->
                refreshTokenService.validateRefreshTokenAndGetUser(generatedToken)
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("validateRefreshTokenAndGetUser - 실패: 만료된 토큰")
    void validateRefreshToken_expiredToken_throwsException() {
        RefreshToken expired = RefreshToken.builder()
                .token(generatedToken)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();
        when(refreshTokenRepository.findById(generatedToken)).thenReturn(Optional.of(expired));

        CustomException ex = assertThrows(CustomException.class, () ->
                refreshTokenService.validateRefreshTokenAndGetUser(generatedToken)
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_REFRESH_TOKEN_EXPIRED);
        verify(refreshTokenRepository, times(1)).delete(expired);
    }

    @Test
    @DisplayName("deleteByUser - 성공: 사용자로 토큰 삭제")
    void deleteByUser_success() {
        refreshTokenService.deleteByUser(testUser);
        verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
    }
}
