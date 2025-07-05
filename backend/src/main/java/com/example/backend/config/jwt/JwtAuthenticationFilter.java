package com.example.backend.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    @Value("${jwt.access-name}")
    private String accessName;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (
                //swagger 관련 경로
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||

                // 인증 관련 허용 경로
                path.startsWith("/api/email") || // 이메일 인증
                path.startsWith("/api/auth/reactive") ||
                path.startsWith("/api/auth/user/signup") ||
                path.startsWith("/api/auth/user/login") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (accessName.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null && tokenProvider.validateToken(token)) {

            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        }

        filterChain.doFilter(request, response);
    }
}
