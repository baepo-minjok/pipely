package com.example.backend.auth.email.model;

import com.example.backend.auth.user.model.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 토큰 엔티티")
public class VerificationToken {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Schema(
            description = "고유 토큰 값(UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    private UUID token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(
            description = "연관된 사용자 정보",
            hidden = true,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Users user;

    @Column(nullable = false)
    @Schema(
            description = "토큰 만료 일시",
            example = "2025-07-10T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime expiryDate;
}