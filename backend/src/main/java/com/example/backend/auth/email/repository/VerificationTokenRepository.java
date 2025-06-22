package com.example.backend.auth.email.repository;

import com.example.backend.auth.email.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
}
