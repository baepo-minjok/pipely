package com.example.backend.jenkins.info.model;

import com.example.backend.auth.user.model.Users;
import com.example.backend.converter.CryptoConverter;
import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.pipeline.PipelineScript;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Schema(name = "JenkinsInfo", description = "사용자의 Jenkins 서버 연결 정보를 저장하는 엔티티")
@Entity
@Table(name = "jenkins_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JenkinsInfo {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "JenkinsInfo 고유 식별자 (UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    @Schema(
            description = "Jenkins 연결 이름 (사용자 지정)",
            example = "My Jenkins Server",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @NotBlank
    @Schema(
            description = "Jenkins 서버 설명",
            example = "회사 CI 서버",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    @NotBlank
    @Column(name = "jenkins_id", nullable = false)
    @Schema(
            description = "Jenkins 관리 콘솔 아이디 (계정 정보)",
            example = "admin_user",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String jenkinsId;

    @NotBlank
    @Column(name = "api_token", nullable = false)
    @Convert(converter = CryptoConverter.class)
    @Schema(
            description = "Jenkins API 토큰 (암호화해서 저장)",
            example = "encryptedTokenString",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String apiToken;

    @NotBlank
    @Column(name = "uri", nullable = false)
    @Schema(
            description = "Jenkins 서버 접근 URI",
            example = "https://jenkins.example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String uri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "연결된 사용자 정보 (내부 참조)", hidden = true)
    private Users user;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(
            description = "레코드 생성 일시 (ISO-8601 형식)",
            example = "2025-07-02T14:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "jenkinsInfo", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "관련된 Freestyle 잡 리스트", hidden = true)
    private List<FreeStyle> freeStyleList = new ArrayList<>();

    @OneToMany(mappedBy = "jenkinsInfo", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "관련된 pipelineScript 잡 리스트", hidden = true)
    private List<PipelineScript> pipelineScriptList = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
