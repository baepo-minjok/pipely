package com.example.backend.auth.token.controller;

import com.example.backend.auth.token.service.RefreshTokenService;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RefreshTokenController.class)
@AutoConfigureMockMvc(addFilters = false)
class RefreshTokenControllerTest {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String OLD_REFRESH_TOKEN = "old-refresh-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RefreshTokenController controller;
    @MockitoBean
    private RefreshTokenService refreshTokenService;
    @MockitoBean
    private JwtTokenProvider tokenProvider;
    @MockitoBean
    private CookieService cookieService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(controller, "refreshName", REFRESH_COOKIE_NAME);
    }

    @Test
    @DisplayName("POST /api/auth/token/refresh - 성공 시 200, 쿠키 및 본문 반환")
    void refreshAccessToken_success() throws Exception {
        // Mock cookie extraction
        when(cookieService.getCookieValue(any(HttpServletRequest.class), eq(REFRESH_COOKIE_NAME)))
                .thenReturn(OLD_REFRESH_TOKEN);

        // validate, no exception
        doNothing().when(refreshTokenService).validateRefreshTokenAndGetUser(OLD_REFRESH_TOKEN);

        // Authentication
        Authentication auth = mock(Authentication.class);
        when(tokenProvider.getAuthentication(OLD_REFRESH_TOKEN)).thenReturn(auth);

        // New tokens
        when(refreshTokenService.createRefreshToken(auth)).thenReturn(NEW_REFRESH_TOKEN);
        when(tokenProvider.createAccessToken(auth)).thenReturn(NEW_ACCESS_TOKEN);

        // Build cookies
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", NEW_ACCESS_TOKEN)
                .path("/")
                .httpOnly(true)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, NEW_REFRESH_TOKEN)
                .path("/")
                .httpOnly(true)
                .build();

        when(cookieService.buildAccessCookie(NEW_ACCESS_TOKEN)).thenReturn(accessCookie);
        when(cookieService.buildRefreshCookie(NEW_REFRESH_TOKEN)).thenReturn(refreshCookie);

        mockMvc.perform(post("/api/auth/token/refresh")
                        .cookie(new Cookie(REFRESH_COOKIE_NAME, OLD_REFRESH_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        accessCookie.toString(), refreshCookie.toString()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("new access token success"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/token/refresh - RefreshToken 유효하지 않음 시 400")
    void refreshAccessToken_invalidToken() throws Exception {
        // Mock cookie extraction
        when(cookieService.getCookieValue(any(HttpServletRequest.class), eq(REFRESH_COOKIE_NAME)))
                .thenReturn(OLD_REFRESH_TOKEN);

        // validation throws exception
        doThrow(new CustomException(ErrorCode.USER_REFRESH_TOKEN_INVALID))
                .when(refreshTokenService).validateRefreshTokenAndGetUser(OLD_REFRESH_TOKEN);

        mockMvc.perform(post("/api/auth/token/refresh")
                        .cookie(new Cookie(REFRESH_COOKIE_NAME, OLD_REFRESH_TOKEN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("USER_REFRESH_TOKEN_INVALID_400"))
                .andExpect(jsonPath("$.error.message").exists());
    }
}
