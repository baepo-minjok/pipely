package com.example.backend.auth.user.repository;

import com.example.backend.auth.user.model.DormantActivationToken;
import com.example.backend.auth.user.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DormantActivationTokenRepository extends JpaRepository<DormantActivationToken, Long> {
    Optional<DormantActivationToken> findByToken(String token);

    void deleteByUser(Users user);
}
