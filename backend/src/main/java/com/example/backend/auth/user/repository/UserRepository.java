package com.example.backend.auth.user.repository;

import com.example.backend.auth.user.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {

    Page<Users> findByStatusAndLastLoginBefore(Users.UserStatus status, LocalDateTime before, Pageable pageable);

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);
}
