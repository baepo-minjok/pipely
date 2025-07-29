package com.example.backend.jenkins.job.model;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@Schema(
        name = "Pipeline",
        description = "Jenkins 파이프라인(Job) 엔티티"
)
public class Pipeline {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "파이프라인의 고유 UUID",
            example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a"
    )
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    @Schema(
            description = "Jenkins Job 이름",
            example = "my-job"
    )
    private String name;

    @Column(name = "is_deleted", nullable = false)
    @Schema(
            description = "삭제 여부",
            example = "false"
    )
    private Boolean isDeleted;

    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "생성 시간 (ISO 8601 형식)",
            example = "2024-07-16T15:32:10"
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(
            description = "수정 시간 (ISO 8601 형식)",
            example = "2024-07-16T15:45:00"
    )
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @Schema(
            description = "삭제 시간 (ISO 8601 형식)",
            example = "2024-07-16T16:00:00"
    )
    private LocalDateTime deletedAt;

    @Column(name = "latest_version_id")
    @Schema(
            description = "가장 최신 버전의 Id",
            example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a"
    )
    private UUID latestVersionId;

    @Column(name = "build_status")
    @Schema(
            description = "빌드 성공 여부 (true: 성공, false: 실패, null: 빌드 전 또는 미실행)",
            example = "true"
    )
    private BuildState buildState;

    private LocalDateTime latestBuildTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    @Schema(
            description = "Jenkins 서버 정보",
            implementation = JenkinsInfo.class,
            hidden = true
    )
    private JenkinsInfo jenkinsInfo;

    @Builder.Default
    @OrderBy("createdAt ASC")
    @OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(
            description = "파이프라인의 버전 목록",
            implementation = PipelineVersion.class,
            hidden = true
    )
    private List<PipelineVersion> versionList = new ArrayList<>();

    public enum BuildState {
        BUILD_SUCCESS,
        BUILD_FAILURE,
        BUILD_RUNNING,
        BUILD_ABORTED,
        BUILD_INIT
    }

}