package com.example.backend.jenkins.job.model;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "pipeline_version",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_pipeline_version", columnNames = {"pipeline_id", "version"})
        }
)
@Schema(
        name = "PipelineVersion",
        description = "파이프라인의 버전 정보를 저장하는 엔티티. Jenkins Job의 특정 시점 버전 상태와 설정을 기록합니다."
)
public class PipelineVersion {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "파이프라인 버전 ID (UUID)",
            example = "3e6c84f7-7fd2-4f57-8015-3b45528d15df"
    )
    private UUID id;

    @Column(name = "version", nullable = false)
    @Schema(
            description = "파이프라인 버전 이름",
            example = "버전1"
    )
    private String name;

    @Column(name = "description", length = 255)
    @Schema(
            description = "Job 설명",
            example = "릴리즈 자동화 스크립트 v1.0"
    )
    private String description;

    @Column(name = "is_triggered", nullable = false)
    @Schema(
            description = "Git webhook 트리거 사용 여부",
            example = "true"
    )
    private Boolean isTriggered;

    @Column(name = "schedule", length = 100)
    @Schema(
            description = "스케줄(cron) 설정 값",
            example = "매일 오후 10시 30분"
    )
    private String schedule;

    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "버전 생성 일시 (ISO 8601)",
            example = "2025-07-15T15:23:00"
    )
    private LocalDateTime createdAt;

    @Lob
    @Schema(
            description = "Jenkins XML Config 전체 문자열",
            example = "<project>...</project>"
    )
    private String config;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id")
    @Schema(
            description = "이 버전에 해당하는 Script 엔티티 참조",
            implementation = Script.class,
            nullable = true
    )
    private Script script;

    @Builder.Default
    @OneToMany(mappedBy = "pipelineVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Schema(
            description = "해당 버전에서 사용되는 Stage 리스트",
            implementation = Stage.class
    )
    private List<Stage> stageList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id")
    @Schema(
            description = "이 PipelineVersion이 소속된 Pipeline 객체 (내부 매핑용, API 문서에는 숨김)",
            implementation = Pipeline.class,
            hidden = true
    )
    private Pipeline pipeline;
}
