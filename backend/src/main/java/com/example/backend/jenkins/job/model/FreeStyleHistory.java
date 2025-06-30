package com.example.backend.jenkins.job.model;

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
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String jobName;

    private String description;

    private String projectUrl;

    private String projectDisplayName;

    private Boolean githubTrigger;

    private String repositoryUrl;

    private String branch;

    private String script;

    private String config;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_style_id", nullable = false)
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
}
