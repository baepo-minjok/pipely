package com.example.backend.auth.email.model;

import com.example.backend.auth.user.model.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

    @Id
    private String token;                  // UUID
    @OneToOne(fetch = FetchType.LAZY)
    private Users user;
    private LocalDateTime expiryDate;
}