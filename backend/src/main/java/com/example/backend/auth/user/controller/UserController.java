package com.example.backend.auth.user.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.PasswordResetDto.PasswordResetRequest;
import com.example.backend.auth.user.model.dto.PasswordResetDto.PasswordResetSubmission;
import com.example.backend.auth.user.model.dto.UserRequestDto.LoginRequest;
import com.example.backend.auth.user.model.dto.UserRequestDto.SignupDto;
import com.example.backend.auth.user.service.PasswordResetService;
import com.example.backend.auth.user.service.RefreshTokenService;
import com.example.backend.auth.user.service.UserService;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.BaseResponse;
import com.example.backend.handler.CookieHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CookieHandler cookieHandler;
    private final PasswordResetService passwordResetService;

    @Value("${jwt.access-name}")
    private String accessName;

    @Value("${jwt.refresh-name}")
    private String refreshName;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<String>> login(@RequestBody @Valid LoginRequest req) {
        // 인증 객체 생성
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
        Authentication auth = authManager.authenticate(authToken);

        // User 정보 가져옴
        Users user = (Users) auth.getPrincipal();

        // 마지막 로그인 시간 갱신
        userService.setLastLogin(user);

        // access, refresh 토큰 생성
        String accessToken = tokenProvider.createAccessToken(auth);
        String refreshToken = refreshTokenService.createRefreshToken(auth);

        // Cookie 생성
        ResponseCookie accessCookie = cookieHandler.buildAccessCookie(accessToken);
        ResponseCookie refreshCookie = cookieHandler.buildRefreshCookie(refreshToken);

        // 쿠키와 응답 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(BaseResponse.success("login success"));
    }


    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<String>> signup(@RequestBody @Valid SignupDto req) {
        userService.registerUser(req);
        return ResponseEntity.ok(BaseResponse.success("signup success"));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        // DB의 refreshToken 삭제
        refreshTokenService.deleteByUser(user);

        // Cookie 만료
        ResponseCookie deleteAccess = cookieHandler.deleteCookie(accessName);
        ResponseCookie deleteRefresh = cookieHandler.deleteCookie(refreshName);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                .body(BaseResponse.success("logout success"));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<BaseResponse<String>> withdraw(Authentication authentication) {

        // 회원 탈퇴 (soft delete)
        userService.withdrawCurrentUser((Users) authentication.getPrincipal());

        // 쿠키 삭제
        ResponseCookie deleteAccess = cookieHandler.deleteCookie(accessName);
        ResponseCookie deleteRefresh = cookieHandler.deleteCookie(refreshName);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                .body(BaseResponse.success("withdraw success"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<String>> refreshAccessToken(@CookieValue(name = "REFRESH", required = false) String refreshToken) {

        refreshTokenService.validateRefreshTokenAndGetUser(refreshToken);

        Authentication authentication = tokenProvider.getAuthentication(refreshToken);

        String newRefreshToken = refreshTokenService.createRefreshToken(authentication);
        String newAccessToken = tokenProvider.createAccessToken(authentication);

        ResponseCookie accessCookie = cookieHandler.buildAccessCookie(newAccessToken);
        ResponseCookie refreshCookie = cookieHandler.buildRefreshCookie(newRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(BaseResponse.success("new access token success"));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<BaseResponse<String>> requestPasswordReset(
            @RequestBody @Valid PasswordResetRequest req) {

        passwordResetService.createPasswordResetTokenAndSendEmail(req.getEmail());

        return ResponseEntity.ok()
                .body(BaseResponse.success("send password reset email success"));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<BaseResponse<String>> confirmPasswordReset(
            @RequestBody @Valid PasswordResetSubmission req) {
        passwordResetService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(BaseResponse.success("password reset success"));
    }
}