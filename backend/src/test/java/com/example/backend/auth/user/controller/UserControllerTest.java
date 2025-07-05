package com.example.backend.auth.user.controller;

import com.example.backend.auth.token.service.RefreshTokenService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.UserRequestDto.LoginRequest;
import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.auth.user.service.UserService;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.service.CookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authManager;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/auth/user/login - 성공 시 200, 쿠키 및 본문 반환")
    void login_success() throws Exception {
        // 준비
        String email = "user@example.com";
        String password = "password123";
        LoginRequest req = new LoginRequest(email, password);

        // Authentication 모킹
        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Users user = Users.builder().id(UUID.randomUUID()).email(email).build();
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserEntity()).thenReturn(user);

        // 토큰 모킹
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        when(tokenProvider.createAccessToken(auth)).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(auth)).thenReturn(refreshToken);

        // Cookie 생성 모킹
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .httpOnly(true)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .httpOnly(true)
                .build();
        when(cookieService.buildAccessCookie(accessToken)).thenReturn(accessCookie);
        when(cookieService.buildRefreshCookie(refreshToken)).thenReturn(refreshCookie);

        // 마지막 로그인 시간 업데이트
        doNothing().when(userService).setLastLogin(user);

        // 실행 & 검증
        mockMvc.perform(post("/api/auth/user/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        accessCookie.toString(), refreshCookie.toString()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("login success"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/user/login - 인증 실패 시 400, 에러 반환")
    void login_badCredentials() throws Exception {
        // 준비
        String email = "user@example.com";
        String password = "wrong-password";
        LoginRequest req = new LoginRequest(email, password);

        // authManager.authenticate 실패 모킹
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Login fail"));

        // 실행 & 검증
        mockMvc.perform(post("/api/auth/user/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("USER_LOGIN_FAILED_400"))
                .andExpect(jsonPath("$.error.message").exists());
    }


}
