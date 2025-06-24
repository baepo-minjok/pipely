package com.example.backend.auth.token.repository;

import com.example.backend.auth.token.model.RefreshToken;
import com.example.backend.auth.user.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    void deleteByUser(Users user);
}
