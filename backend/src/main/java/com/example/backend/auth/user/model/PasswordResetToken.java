package com.example.backend.auth.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 비밀번호 초기화 토큰 엔티티
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(
        name = "PasswordResetToken",
        description = "사용자의 비밀번호 재설정을 위해 발급되는 일회용 토큰 정보 엔티티입니다."
)
public class PasswordResetToken {

    @Id
    @GeneratedValue
    @Schema(
            description = "토큰 레코드 고유 ID (UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @Column(nullable = false, unique = true)
    @NotNull
    @Schema(
            description = "비밀번호 재설정에 사용되는 고유 토큰 값 (UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    @Schema(
            description = "토큰과 연관된 사용자 엔티티",
            hidden = true
    )
    private Users user;

    @Column(nullable = false, updatable = false)
    @NotNull
    @Schema(
            description = "토큰 생성 일시",
            example = "2025-07-10T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @NotNull
    @Schema(
            description = "토큰 만료 일시",
            example = "2025-07-10T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime expiresAt;

}
