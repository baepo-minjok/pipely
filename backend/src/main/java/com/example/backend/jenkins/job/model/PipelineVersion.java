package com.example.backend.jenkins.job.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pipeline_version",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_pipeline_version", columnNames = {"pipeline_id", "version"})
        })
@Schema(description = "파이프라인의 버전 정보를 저장하는 엔티티")
public class PipelineVersion {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @Schema(description = "파이프라인 버전 ID", example = "3e6c84f7-7fd2-4f57-8015-3b45528d15df")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    @JsonBackReference
    @Schema(description = "연결된 파이프라인 객체", hidden = true)
    private Pipeline pipeline;

    @Column(nullable = false)
    @Schema(description = "버전 번호", example = "1")
    private Integer version;


    @Column(nullable = false)
    @Schema(description = "버전 생성 일시", example = "2025-07-15T15:23:00")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id")
    @Schema(description = "스크립트 객체 참조", example = "Script 엔티티 참조")
    private Script script;

    @Lob
    @Schema(description = "Jenkins XML Config 또는 JSON Config 문자열", example = "<project>...</project>")
    private String config;

    @Column
    @Schema(description = "빌드 성공 여부 (true: 성공, false: 실패, null: 빌드 전)", example = "true")
    private Boolean isSuccessfulBuild;

}
