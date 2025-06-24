package com.example.backend.auth.user.service;

import com.example.backend.auth.user.model.Users;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Users user;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(Users user) {
        this.user = user;
        // 예: users.getRoles()가 "USER,ADMIN" 같은 포맷일 때
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            this.authorities = List.of(user.getRoles().split(",")).stream()
                    .map(roleStr -> {
                        // Spring Security는 GrantedAuthority에 "ROLE_" 접두사가 붙어야 할 수 있음
                        String role = roleStr.startsWith("ROLE_") ? roleStr : "ROLE_" + roleStr;
                        return new SimpleGrantedAuthority(role);
                    })
                    .collect(Collectors.toList());
        } else {
            this.authorities = List.of();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // 로그인 식별자로 이메일을 사용한다면
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 보통 탈퇴나 휴면 등은 loadUserByUsername 단계에서 예외 던지므로 여기서는 항상 true로 두어도 됨
        return true;
    }

    // 편의 메서드: 도메인 Users 반환
    public Users getUserEntity() {
        return this.user;
    }
}
