package com.example.backend.user.controller;

import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.BaseResponse;
import com.example.backend.user.model.dto.LoginRequest;
import com.example.backend.user.model.dto.SignupRequest;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.UserService;
import com.example.backend.user.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final UserRepository userRepository;
    @Value("${jwt.access-expiration}")
    private long accessExpiration;


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
        Authentication auth = authManager.authenticate(authToken);

        String accessToken = tokenProvider.createAccessToken(auth);
        String refreshToken = tokenProvider.createRefreshToken(auth);
        ResponseCookie cookie = ResponseCookie.from("JWT", accessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(accessExpiration))
                .sameSite("Strict")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("ok");
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest req) {
        userService.registerUser(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout() {
        ResponseCookie cookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(BaseResponse.success("logout success"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        return verificationTokenService.validateToken(token)
                .map(user -> {
                    user.setEnabled(true);
                    userRepository.save(user);
                    return ResponseEntity.ok("이메일 인증 성공! 이제 로그인 가능합니다.");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("유효하지 않은 토큰입니다."));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(Authentication authentication) {
        String email = authentication.getName();

        userService.withdrawCurrentUser(email);

        ResponseCookie deleteCookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}