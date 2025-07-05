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
@Table(name = "pipeline_script_history",
        uniqueConstraints = @UniqueConstraint(name = "uq_script_version", columnNames = {"pipeline_script_id", "version"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineScriptHistory {

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
    @JoinColumn(name = "pipeline_script_id", nullable = false)
    private PipelineScript pipelineScript;

    public static PipelineScriptHistory toHistory(PipelineScript job, int version, String config) {
        return PipelineScriptHistory.builder()
                .jobName(job.getJobName())
                .description(job.getDescription())
                .projectUrl(job.getProjectUrl())
                .projectDisplayName(job.getProjectDisplayName())
                .script(job.getScript())
                .githubTrigger(job.getGithubTrigger())
                .version(version)
                .createdAt(LocalDateTime.now())
                .config(config)
                .pipelineScript(job)
                .build();
    }
}
