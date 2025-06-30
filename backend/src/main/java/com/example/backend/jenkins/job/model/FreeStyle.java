package com.example.backend.jenkins.job.model;

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
@Table(
        name = "free_style",
        uniqueConstraints = @UniqueConstraint(columnNames = {"jenkins_info_id", "job_name"})
)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreeStyle {

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

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    private JenkinsInfo jenkinsInfo;

    @OneToMany(mappedBy = "freeStyle", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FreeStyleHistory> historyList = new ArrayList<>();
}
