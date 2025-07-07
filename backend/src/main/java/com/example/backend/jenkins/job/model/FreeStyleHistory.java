package com.example.backend.jenkins.job.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "free_style_history",
        uniqueConstraints = @UniqueConstraint(name = "uq_job_version", columnNames = {"free_style_id", "version"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeStyleHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "FreeStyleHistory 고유 식별자 (UUID)",
            example = "d290f1ee-6c54-4b01-90e6-d701748f0851",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @Column(name = "job_name", nullable = false)
    @Schema(
            description = "Freestyle 잡 이름 (Jenkins 내에서의 Job 식별자)",
            example = "build-and-deploy",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String jobName;

    @Schema(
            description = "Freestyle 잡 설명",
            example = "이 잡은 프로젝트를 빌드하고 배포합니다.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    @Schema(
            description = "프로젝트 관련 URL",
            example = "https://github.com/example/project",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String projectUrl;

    @Schema(
            description = "Jenkins UI에 표시될 프로젝트 이름",
            example = "Example Project",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String projectDisplayName;

    @Schema(
            description = "GitHub Webhook 트리거 활성화 여부",
            example = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean githubTrigger;

    @Schema(
            description = "빌드 대상 Git 저장소 URL",
            example = "https://github.com/example/project.git",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String repositoryUrl;

    @Schema(
            description = "빌드 대상 Git 브랜치",
            example = "main",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String branch;

    @Lob
    @Schema(
            description = "FreeStyle shell script 내용",
            example = "#!/bin/bash\n\necho \"Build started\"\n./gradlew build\n",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String script;

    @Lob
    @Column(nullable = false)
    @Schema(
            description = "Jenkins 잡 구성 XML (config.xml) 내용",
            example = "<project>...</project>",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String config;

    @Column(name = "version", nullable = false)
    @Schema(
            description = "잡 버전 번호",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer version;

    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "이력 생성 일시 (ISO-8601 형식)",
            example = "2025-07-03T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_style_id", nullable = false)
    @Schema(description = "관련된 FreeStyle 잡 정보 (내부 참조)", hidden = true)
    private FreeStyle freeStyle;

    public static FreeStyleHistory toHistory(FreeStyle freeStyle, int version, String config) {
        return FreeStyleHistory.builder()
                .jobName(freeStyle.getJobName())
                .description(freeStyle.getDescription())
                .projectUrl(freeStyle.getProjectUrl())
                .projectDisplayName(freeStyle.getProjectDisplayName())
                .githubTrigger(freeStyle.getGithubTrigger())
                .repositoryUrl(freeStyle.getRepositoryUrl())
                .branch(freeStyle.getBranch())
                .script(freeStyle.getScript())
                .version(version)
                .createdAt(LocalDateTime.now())
                .freeStyle(freeStyle)
                .config(config)
                .build();
    }

    public static FreeStyle toFreeStyle(FreeStyleHistory history, FreeStyle freeStyle) {
        return FreeStyle.builder()
                .id(freeStyle.getId())
                .jobName(history.getJobName())
                .description(history.getDescription())
                .projectUrl(history.getProjectUrl())
                .projectDisplayName(history.getProjectDisplayName())
                .githubTrigger(history.getGithubTrigger())
                .repositoryUrl(history.getRepositoryUrl())
                .branch(history.getBranch())
                .script(history.getScript())
                .jenkinsInfo(freeStyle.getJenkinsInfo())
                .build();
    }
}
