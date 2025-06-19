package com.example.backend.user.service;

import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        log.info("[UserDetailsService] 사용자 인증 요청 수신: username={}", username);

        return userRepository.findById(username)
                .map(users -> {
                    String[] rolesArray = users.getRoles().split(",");
                    log.info("[UserDetailsService] 사용자 정보 조회 성공: email={}, roles={}", users.getEmail(), users.getRoles());

                    return User.builder()
                            .username(users.getEmail())
                            .password(users.getPassword())
                            .roles(rolesArray)
                            .build();
                })
                .orElseThrow(() -> {
                    log.warn("[UserDetailsService] 사용자 조회 실패: 존재하지 않는 username={}", username);
                    return new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + username);
                });
    }

}
