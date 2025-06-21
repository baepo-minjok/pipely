package com.example.backend.auth.user.repository;

import com.example.backend.auth.user.model.RefreshToken;
import com.example.backend.auth.user.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(Users user);
}
