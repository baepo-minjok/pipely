package com.example.backend.auth.user.model;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Schema(
        name = "Users",
        description =
                "시스템 내 등록된 사용자 정보를 나타내는 엔티티 클래스입니다. " +
                        "UUID, 이메일, 상태 정보 및 관련 Jenkins 설정을 포함합니다."
)
@Entity(name = "user")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Users {

    @Schema(description = "사용자 고유 식별자(UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(unique = true, nullable = false)
    @Schema(description = "사용자 이메일 (로그인 및 인증에 사용됩니다)",
            example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Column(nullable = false)
    @Schema(description = "사용자 이름 (실명)",
            example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(nullable = false)
    @Schema(description = "암호화된 비밀번호 (BCrypt로 저장됨)",
            example = "$2a$10$abcdefg...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Column(nullable = false)
    @Schema(description = "휴대폰 번호 (연락처 정보)",
            example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "계정 상태 (ACTIVE, DORMANT, WITHDRAWN, UNVERIFIED)",
            example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserStatus status;

    @Column(nullable = false)
    @Schema(description = "사용자 권한 목록",
            example = "ROLE_USER,ROLE_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roles;

    @Column
    @Schema(description = "OAuth 제공자 (가입 시 OAuth 사용한 경우)",
            example = "google", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String provider;

    @Column
    @Schema(description = "마지막 로그인 시간 (ISO-8601 형식)",
            example = "2025-07-01T12:34:56", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime lastLogin;

    @Column(nullable = false, updatable = false)
    @Schema(description = "계정 생성 시간 (불변)",
            example = "2025-01-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;

    @Column
    @Schema(description = "계정 삭제(탈퇴) 시간 (soft delete)",
            example = "2025-06-30T15:20:30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "사용자와 연결된 Jenkins 설정 정보 목록")
    private List<JenkinsInfo> jenkinsInfoList = new ArrayList<>();

    public enum UserStatus {
        ACTIVE,       // 정상 사용 가능한 상태
        DORMANT,      // 휴면 상태, 로그인 제한
        WITHDRAWN,    // 탈퇴 처리된 계정
        UNVERIFIED    // 이메일 인증 대기 상태
    }
}
