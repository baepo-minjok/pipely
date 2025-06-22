package com.example.backend.auth.user.repository;

import com.example.backend.auth.user.model.RefreshToken;
import com.example.backend.auth.user.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    void deleteByUser(Users user);
}
