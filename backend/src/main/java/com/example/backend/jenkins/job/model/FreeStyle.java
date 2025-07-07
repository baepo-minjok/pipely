package com.example.backend.jenkins.job.model;

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

@Schema(name = "FreeStyle", description = "Jenkins의 Freestyle Job 정보를 저장하는 엔티티")
@Entity
@Table(
        name = "free_style",
        uniqueConstraints = @UniqueConstraint(columnNames = {"jenkins_info_id", "job_name"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeStyle {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "FreeStyle 고유 식별자 (UUID)",
            example = "550e8400-e29b-41d4-a716-446655440000",
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


    @Schema(
            description = "삭제 여부 (소프트 삭제 플래그)",
            example = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean isDeleted;

    @Column(name = "created_at")
    @Schema(
            description = "레코드 생성 일시 (ISO-8601 형식)",
            example = "2025-07-03T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(
            description = "레코드 수정 일시 (ISO-8601 형식)",
            example = "2025-07-03T12:30:00",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @Schema(
            description = "레코드 삭제 일시 (ISO-8601 형식)",
            example = "2025-07-03T13:00:00",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    @Schema(
            description = "연결된 Jenkins 서버 정보 (내부 참조)",
            hidden = true
    )
    private JenkinsInfo jenkinsInfo;

    @OneToMany(mappedBy = "freeStyle", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(
            description = "Freestyle 잡의 실행 이력 목록 (내부 참조)",
            hidden = true
    )
    private List<FreeStyleHistory> historyList = new ArrayList<>();
    @OneToMany(mappedBy = "freeStyle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobNotification> jobNotifications = new ArrayList<>();
}
