package com.example.backend.jenkins.job.model.pipeline;

import com.example.backend.jenkins.info.model.JenkinsInfo;
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

@Entity
@Table(name = "pipeline_script_job",
        uniqueConstraints = @UniqueConstraint(name = "uq_script_job", columnNames = {"jenkins_info_id", "job_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineScript {

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

    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    private JenkinsInfo jenkinsInfo;


    @OneToMany(mappedBy = "pipelineScript", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PipelineScriptHistory> historyList = new ArrayList<>();
}

