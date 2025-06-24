package com.example.backend.auth.user.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        return userRepository.findByEmail(username)
                .map(users -> {
                    String[] rolesArray = users.getRoles().split(",");
                    log.info("[UserDetailsService] 사용자 정보 조회 성공: email={}, roles={}", users.getEmail(), users.getRoles());
                    Users.UserStatus userStatus = users.getStatus();
                    if (userStatus.equals(Users.UserStatus.DORMANT)) {
                        throw new CustomException(ErrorCode.USER_DORMANT);
                    } else if (userStatus.equals(Users.UserStatus.UNVERIFIED)) {
                        throw new CustomException(ErrorCode.USER_UNVERIFIED);
                    } else if (userStatus.equals(Users.UserStatus.WITHDRAWN)) {
                        throw new CustomException(ErrorCode.USER_WITHDRAWN);
                    }
                    /*User.builder()
                            .username(users.getEmail())
                            .password(users.getPassword())
                            .roles(rolesArray)
                            .build();*/
                    return new CustomUserDetails(users);
                })
                .orElseThrow(() -> {
                    log.warn("[UserDetailsService] 사용자 조회 실패: 존재하지 않는 username={}", username);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });
    }

}
