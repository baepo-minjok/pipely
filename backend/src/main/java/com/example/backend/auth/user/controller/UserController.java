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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "User API", description = "User 관련 API")
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

    @Operation(
            summary = "사용자 로그인",
            description = """
                    사용자가 이메일과 비밀번호로 로그인합니다.
                    1. 인증 정보 확인 후 마지막 로그인 시간을 갱신합니다.
                    2. AccessToken과 RefreshToken을 생성하여 각각 HttpOnly 쿠키에 담아 응답 헤더에 설정합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "인증 실패 (이메일 또는 비밀번호 불일치)")
    })
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

    @Operation(
            summary = "사용자 회원가입",
            description = """
                    새로운 사용자를 등록하고, 인증 이메일을 발송합니다.
                    1. 이메일 중복 여부 및 비밀번호 복잡도 등 유효성 검사를 수행합니다.
                    2. 사용자 정보를 저장하고, 인증 토큰이 포함된 이메일을 발송합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "회원가입 실패")
    })
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<String>> signup(
            @RequestBody @Valid SignupDto req
    ) {
        userService.registerUser(req);
        return ResponseEntity.ok()
                .body(BaseResponse.success("signup success"));
    }

    @Operation(
            summary = "사용자 로그아웃",
            description = """
                    인증된 사용자의 로그아웃을 처리합니다.
                    AccessToken으로부터 인증된 사용자 정보를 기반으로 서버에 저장된 RefreshToken을 삭제하고,
                    클라이언트에 저장된 인증 관련 쿠키(AccessToken, RefreshToken)를 만료시켜 로그아웃을 완료합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
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

    @Operation(
            summary = "사용자 탈퇴",
            description = """
                    인증된 사용자의 회원 탈퇴를 처리합니다.
                    soft-delete 방식으로 User 엔티티의 status를 WITHDRAWN으로 바꿉니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
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