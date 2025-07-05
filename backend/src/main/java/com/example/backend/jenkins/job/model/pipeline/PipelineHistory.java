package com.example.backend.jenkins.job.model.pipeline;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pipeline_history",
        uniqueConstraints = @UniqueConstraint(name = "uq_pipeline_version", columnNames = {"pipeline_id", "version"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String jobName;
    private String description;
    private String projectUrl;
    private String projectDisplayName;

    @Lob
    private String script;

    private Boolean githubTrigger;

    @Lob
    private String config;

    private Integer version;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    public static PipelineHistory toHistory(Pipeline job, int version, String config) {
        return PipelineHistory.builder()
                .jobName(job.getJobName())
                .description(job.getDescription())
                .projectUrl(job.getProjectUrl())
                .projectDisplayName(job.getProjectDisplayName())
                .script(job.getScript())
                .githubTrigger(job.getGithubTrigger())
                .version(version)
                .createdAt(LocalDateTime.now())
                .config(config)
                .pipeline(job)
                .build();
    }
}
