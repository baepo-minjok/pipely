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

@Tag(name = "Refresh Token API", description = "AccessToken 재발급을 위한 Refresh Token 처리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
public class RefreshTokenController {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    @Value("${jwt.refresh-name}")
    private String refreshName;

    @Operation(
            summary = "AccessToken 재발급",
            description = """
                    클라이언트가 보유한 RefreshToken을 통해 새로운 AccessToken과 RefreshToken을 재발급합니다.
                    
                    - 클라이언트는 RefreshToken을 HTTP 요청의 쿠키로 전송해야 합니다.
                    - 서버는 RefreshToken을 검증한 뒤 새로운 AccessToken, RefreshToken을 생성합니다.
                    - 생성된 토큰은 다시 쿠키로 클라이언트에 전달되며, 기존 쿠키를 덮어씁니다.
                    - RefreshToken이 유효하지 않거나 만료되었을 경우, 400 에러를 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AccessToken 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "RefreshToken이 유효하지 않거나 만료됨")
    })
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<String>> refreshAccessToken(
            HttpServletRequest request
    ) {
        // 요청에서 refreshToken 쿠키 값을 추출
        String refreshToken = cookieService.getCookieValue(request, refreshName);

        // 토큰 유효성 검증 및 사용자 조회
        refreshTokenService.validateRefreshTokenAndGetUser(refreshToken);

        // 사용자 인증 객체 생성
        Authentication authentication = tokenProvider.getAuthentication(refreshToken);

        // 토큰 재발급
        String newRefreshToken = refreshTokenService.createRefreshToken(authentication);
        String newAccessToken = tokenProvider.createAccessToken(authentication);

        // 쿠키 생성
        ResponseCookie accessCookie = cookieService.buildAccessCookie(newAccessToken);
        ResponseCookie refreshCookie = cookieService.buildRefreshCookie(newRefreshToken);

        // 쿠키 설정 및 응답 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(BaseResponse.success("new access token success"));
    }
}
