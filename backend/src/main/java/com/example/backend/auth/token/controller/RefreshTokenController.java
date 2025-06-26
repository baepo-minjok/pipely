package com.example.backend.auth.token.controller;

import com.example.backend.auth.token.service.RefreshTokenService;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.BaseResponse;
import com.example.backend.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
public class RefreshTokenController {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    @Value("${jwt.refresh-name}")
    private String refreshName;

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<String>> refreshAccessToken(HttpServletRequest request) {

        String refreshToken = cookieService.getCookieValue(request, refreshName);

        refreshTokenService.validateRefreshTokenAndGetUser(refreshToken);

        Authentication authentication = tokenProvider.getAuthentication(refreshToken);

        String newRefreshToken = refreshTokenService.createRefreshToken(authentication);
        String newAccessToken = tokenProvider.createAccessToken(authentication);

        ResponseCookie accessCookie = cookieService.buildAccessCookie(newAccessToken);
        ResponseCookie refreshCookie = cookieService.buildRefreshCookie(newRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(BaseResponse.success("new access token success"));
    }
}
