package com.example.backend.jenkins.job.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "pipeline_stage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pipeline_version_id", "order_index"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Stage",
        description = "Jenkins Pipeline 내 Stage(단계) 정보를 저장하는 엔티티"
)
public class Stage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "Stage의 고유 식별자 (UUID)",
            example = "7e6adf14-8153-41e7-aeb1-2f97782f3b0d"
    )
    private UUID id;

    @Column(name = "order_index", nullable = false)
    @Schema(
            description = "파이프라인 내에서의 Stage 순서 (0부터 시작)",
            example = "0"
    )
    private Integer orderIndex;

    @Column(name = "name", nullable = false, length = 50)
    @Schema(
            description = "Stage의 이름 (보통 대문자, 알파벳·숫자·언더바 등)",
            example = "BUILD"
    )
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_version_id", nullable = false)
    @Schema(
            description = "해당 Stage가 속한 PipelineVersion 엔티티",
            implementation = PipelineVersion.class,
            hidden = true
    )
    private PipelineVersion pipelineVersion;
}
