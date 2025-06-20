package com.example.backend.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {
        log.info("[OAuth2 Login] 요청 수신: provider={}, clientName={}",
                request.getClientRegistration().getRegistrationId(),
                request.getClientRegistration().getClientName());

        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(request);

        Map<String, Object> originalAttributes = oauthUser.getAttributes();
        Map<String, Object> attributes = new HashMap<>(originalAttributes);

        String githubId = attributes.get("id").toString();
        String login    = (String) attributes.get("login");

        String email = fetchEmailIfNeeded(attributes, login);
        attributes.put("email", email);

        OAuth2User user = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "login"
        );

        log.info("[OAuth2 Login] OAuth2User 생성 완료: authorities={}, nameAttributeKey={}",
                user.getAuthorities(), "login");

        return user;
    }

    private String fetchEmailIfNeeded(Map<String, Object> attributes, String login) {
        Object emailObj = attributes.get("email");
        log.info("[OAuth2 Email Fetch] 실행: login={}, 기존 email 속성={}", login, emailObj);

        if (emailObj instanceof String && !((String) emailObj).isBlank()) {
            log.info("[OAuth2 Email Fetch] 유효한 이메일 존재. 그대로 사용: {}", emailObj);
            return (String) emailObj;
        }

        String fakeEmail = login + "@github.com";
        attributes.put("email", fakeEmail);
        log.info("[OAuth2 Email Fetch] 이메일 없음 또는 비어 있음. 임시 이메일 생성: {}", fakeEmail);
        return fakeEmail;
    }



}
