package com.example.backend.jenkins.job.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "version_stage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pipeline_version_id", "order_index"})
)
public class VersionStage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "order_index", nullable = false)
    @Schema(
            description = "파이프라인 내에서의 Stage 순서 (0부터 시작)",
            example = "0"
    )
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    @Schema(
            description = "해당 Stage가 속한 PipelineVersion 엔티티",
            implementation = Stage.class,
            hidden = true
    )
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_version_id", nullable = false)
    @Schema(
            description = "해당 Stage가 속한 PipelineVersion 엔티티",
            implementation = PipelineVersion.class,
            hidden = true
    )
    private PipelineVersion pipelineVersion;
}
