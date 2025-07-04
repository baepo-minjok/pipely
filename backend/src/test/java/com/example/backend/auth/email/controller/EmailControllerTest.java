package com.example.backend.auth.email.controller;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.service.UserService;
import com.example.backend.config.jwt.JwtAuthenticationFilter;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
    }


    @Test
    @DisplayName("이메일 인증 성공 시 200 OK 응답 반환")
    @WithMockUser
    void verifyEmail_success() throws Exception {
        when(emailService.validateToken(testToken)).thenReturn(testUser);
        doNothing().when(userService).setUserStatusActive(testUser);

        mockMvc.perform(get("/api/email/verify-email")
                        .param("token", testToken.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("email verified"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(emailService, times(1)).validateToken(testToken);
        verify(userService, times(1)).setUserStatusActive(testUser);
    }

    @Test
    @DisplayName("유효하지 않거나 만료된 토큰으로 인한 이메일 인증 실패 시 400 Bad Request 반환")
    void verifyEmail_invalidToken_returnsBadRequest() throws Exception {
        when(emailService.validateToken(testToken))
                .thenThrow(new CustomException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID));

        mockMvc.perform(get("/api/email/verify-email")
                        .param("token", testToken.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("EMAIL_VERIFICATION_TOKEN_INVALID_400"))
                .andExpect(jsonPath("$.error.message").exists());

        verify(emailService, times(1)).validateToken(testToken);
        verify(userService, never()).setUserStatusActive(any(Users.class));
    }

    @Test
    @DisplayName("토큰 파라미터 누락 시 400 Bad Request 반환")
    void verifyEmail_missingTokenParameter_returnsBadRequest() throws Exception {

        mockMvc.perform(get("/api/email/verify-email")) // 토큰 파라미터 없이 요청
                .andExpect(status().isBadRequest()) // HTTP 상태 코드 400 Bad Request 검증
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.code").value("MISSING_PARAMETER_400"));

        verify(emailService, never()).validateToken(any(UUID.class));
        verify(userService, never()).setUserStatusActive(any(Users.class));
    }
}