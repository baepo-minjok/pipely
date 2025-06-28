package com.example.backend.auth.user.repository;

import com.example.backend.auth.user.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {

    Page<Users> findByStatusAndLastLoginBefore(Users.UserStatus status, LocalDateTime before, Pageable pageable);

    @Query("SELECT u FROM user u LEFT JOIN FETCH u.jenkinsInfoList WHERE u.email = :email")
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);
}
