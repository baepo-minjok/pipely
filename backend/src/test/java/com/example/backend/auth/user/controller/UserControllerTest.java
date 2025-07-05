package com.example.backend.auth.user.controller;

import com.example.backend.auth.token.service.RefreshTokenService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.UserRequestDto.LoginRequest;
import com.example.backend.auth.user.model.dto.UserRequestDto.SignupDto;
import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.auth.user.service.UserService;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.CookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    private static final String ACCESS_COOKIE_NAME = "accessToken";
    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserController controller;
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

    private Users testUser;
    private UsernamePasswordAuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        // inject @Value fields for logout endpoint
        ReflectionTestUtils.setField(controller, "accessName", ACCESS_COOKIE_NAME);
        ReflectionTestUtils.setField(controller, "refreshName", REFRESH_COOKIE_NAME);

        // prepare a test user and Authentication for logout
        testUser = Users.builder()
                .id(UUID.randomUUID())
                .email("logout@example.com")
                .build();
        authToken = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
    }

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

    @Test
    @DisplayName("POST /api/auth/user/signup - valid 위반 시 400 및 에러 메시지 반환")
    void signup_valid() throws Exception {

        String email = "user@example.com";
        String password = "password123";
        String name = "user";
        String phoneNumber = "010-3923-2943";

        SignupDto req = new SignupDto(email, password, name, phoneNumber);

        mockMvc.perform(post("/api/auth/user/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED_400"))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/user/signup - 성공 시 200, SUCCESS 및 메시지 반환")
    void signup_success() throws Exception {
        // given
        String email = "newuser@example.com";
        String password = "StrongP@ssw0rd";
        String name = "New User";
        String phoneNumber = "010-1234-5678";
        SignupDto req = new SignupDto(email, password, name, phoneNumber);

        // userService.registerUser 는 예외 없이 void 리턴
        doNothing().when(userService).registerUser(any(SignupDto.class));

        // when & then
        mockMvc.perform(post("/api/auth/user/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("signup success"))
                .andExpect(jsonPath("$.error").doesNotExist());

    }

    @Test
    @DisplayName("POST /api/auth/user/signup - 회원가입 실패 시 400 및 에러 메시지 반환")
    void signup_fail() throws Exception {
        // given
        String email = "newuser@example.com";
        String password = "StrongP@ssw0rd";
        String name = "New User";
        String phoneNumber = "010-1234-5678";
        SignupDto req = new SignupDto(email, password, name, phoneNumber);

        doThrow(new CustomException(ErrorCode.USER_EMAIL_DUPLICATED)).when(userService).registerUser(req);

        // when & then
        mockMvc.perform(post("/api/auth/user/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("USER_EMAIL_DUPLICATED_400"))
                .andExpect(jsonPath("$.error.message").exists());

    }

    @Test
    @DisplayName("POST /api/auth/user/logout - 성공 시 200, 쿠키 만료 및 메시지 반환")
    void logout_success() throws Exception {
        // expired-cookie mocks
        ResponseCookie deleteAccess = ResponseCookie.from(ACCESS_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .build();
        ResponseCookie deleteRefresh = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .build();
        when(cookieService.deleteCookie(ACCESS_COOKIE_NAME)).thenReturn(deleteAccess);
        when(cookieService.deleteCookie(REFRESH_COOKIE_NAME)).thenReturn(deleteRefresh);

        mockMvc.perform(post("/api/auth/user/logout")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    List<String> setCookieHeaders = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);

                    // accessToken 검사
                    assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("accessToken=;")));
                    assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("Max-Age=0")));

                    // refreshToken 검사
                    assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("refreshToken=;")));
                    assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("Max-Age=0")));
                })
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("logout success"));

    }

    @Test
    @DisplayName("DELETE /api/auth/user/withdraw - 성공 시 200, 쿠키 만료 및 메시지 반환")
    void withdraw_success() throws Exception {
        // expired-cookie mocks
        ResponseCookie deleteAccess = ResponseCookie.from(ACCESS_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .build();
        ResponseCookie deleteRefresh = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .build();
        when(cookieService.deleteCookie(ACCESS_COOKIE_NAME)).thenReturn(deleteAccess);
        when(cookieService.deleteCookie(REFRESH_COOKIE_NAME)).thenReturn(deleteRefresh);

        doNothing().when(userService).withdrawCurrentUser(any(Users.class));

        mockMvc.perform(delete("/api/auth/user/withdraw")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    List<String> setCookieHeaders = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);

                    // accessToken 검사
                    assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("accessToken=;")));
                    assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("Max-Age=0")));

                    // refreshToken 검사
                    assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("refreshToken=;")));
                    assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("Max-Age=0")));
                })
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("withdraw success"));
    }
}
