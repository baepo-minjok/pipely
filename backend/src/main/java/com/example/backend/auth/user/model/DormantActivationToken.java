package com.example.backend.auth.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dormant_activation_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계정 재활성화 인증 토큰 엔티티")
public class DormantActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "토큰 레코드 고유 ID (UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    @Schema(
            description = "비밀번호 재설정에 사용되는 고유 토큰 값 (UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(
            description = "토큰과 연관된 사용자 엔티티",
            hidden = true
    )
    private Users user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(
            description = "토큰 생성 일시",
            example = "2025-07-10T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Schema(
            description = "토큰 만료 일시",
            example = "2025-07-10T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime expiresAt;
}
