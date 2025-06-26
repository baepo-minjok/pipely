package com.example.backend.auth.user.controller;

import com.example.backend.auth.token.service.RefreshTokenService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.UserRequestDto.LoginRequest;
import com.example.backend.auth.user.model.dto.UserRequestDto.SignupDto;
import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.auth.user.service.UserService;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.BaseResponse;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.CookieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class UserController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

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

        Users user;
        if (auth.getPrincipal() instanceof CustomUserDetails) {
            user = ((CustomUserDetails) auth.getPrincipal()).getUserEntity();
        } else {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        // 마지막 로그인 시간 갱신
        userService.setLastLogin(user);

        // access, refresh 토큰 생성
        String accessToken = tokenProvider.createAccessToken(auth);
        String refreshToken = refreshTokenService.createRefreshToken(auth);

        // Cookie 생성
        ResponseCookie accessCookie = cookieService.buildAccessCookie(accessToken);
        ResponseCookie refreshCookie = cookieService.buildRefreshCookie(refreshToken);

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
    public ResponseEntity<BaseResponse<String>> logout(
            @AuthenticationPrincipal(expression = "userEntity") Users user
    ) {

        // DB의 refreshToken 삭제
        refreshTokenService.deleteByUser(user);
        // Cookie 만료
        ResponseCookie deleteAccess = cookieService.deleteCookie(accessName);
        ResponseCookie deleteRefresh = cookieService.deleteCookie(refreshName);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                .body(BaseResponse.success("logout success"));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<BaseResponse<String>> withdraw(
            @AuthenticationPrincipal(expression = "userEntity") Users user
    ) {

        // 회원 탈퇴 (soft delete)
        userService.withdrawCurrentUser(user);

        // 쿠키 삭제
        ResponseCookie deleteAccess = cookieService.deleteCookie(accessName);
        ResponseCookie deleteRefresh = cookieService.deleteCookie(refreshName);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                .body(BaseResponse.success("withdraw success"));
    }

}