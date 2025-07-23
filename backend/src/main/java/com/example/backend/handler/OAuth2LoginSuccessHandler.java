package com.example.backend.handler;

import com.example.backend.auth.user.service.UserService;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        boolean isExist = userService.isExist(oAuth2User);

        if (!isExist) {
            String oAuth2Token = jwtTokenProvider.createOAuth2Token(oAuth2User, registrationId);
            ResponseCookie cookie = cookieService.buildOAuth2Cookie(oAuth2Token);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.sendRedirect("http://localhost:5173/user/oAuth");
        } else {

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            ResponseCookie cookie = cookieService.buildAccessCookie(accessToken);
            ResponseCookie refreshCookie = cookieService.buildRefreshCookie(refreshToken);

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            response.sendRedirect("http://localhost:5173");
        }
    }
}
