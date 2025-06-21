package com.example.backend.auth.user.repository;

import com.example.backend.auth.user.model.PasswordResetToken;
import com.example.backend.auth.user.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteAllByUser(Users user);
}
