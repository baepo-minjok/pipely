package com.example.backend.auth.token.model;

import com.example.backend.auth.user.model.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "RefreshToken",
        description = "리프레쉬 토큰 정보 엔티티입니다."
)
public class RefreshToken {

    @Id
    @Column(nullable = false, unique = true, length = 512)
    @Schema(
            description = "고유 토큰 값(UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(
            description = "연관된 사용자 정보",
            hidden = true
    )
    private Users user;

    @Column(nullable = false)
    @Schema(
            description = "토큰 만료 일시",
            example = "2025-07-10T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime expiryDate;

    @Column(nullable = false, updatable = false)
    @Schema(
            description = "토큰 생성 일시",
            example = "2025-07-10T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
