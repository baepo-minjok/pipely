package com.example.backend.auth.token.controller;

import com.example.backend.auth.token.service.RefreshTokenService;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.BaseResponse;
import com.example.backend.service.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Refresh Token API", description = "Refresh Token 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
public class RefreshTokenController {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    @Value("${jwt.refresh-name}")
    private String refreshName;

    @Operation(summary = "AccessToken 재발급", description = "Refresh Token 검증 후 AccessToken 발급하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<String>> refreshAccessToken(
            HttpServletRequest request
    ) {

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
