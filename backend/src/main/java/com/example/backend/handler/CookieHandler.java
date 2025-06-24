package com.example.backend.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieHandler {

    @Value("${jwt.access-name}")
    private String accessName;

    @Value("${jwt.refresh-name}")
    private String refreshName;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public ResponseCookie deleteCookie(String name) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict");

        return builder.build();
    }

    public ResponseCookie buildAccessCookie(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(accessName, token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(accessExpiration))
                .sameSite("Strict");

        return builder.build();
    }

    public ResponseCookie buildRefreshCookie(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(refreshName, token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(refreshExpiration))
                .sameSite("Strict");

        return builder.build();
    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
