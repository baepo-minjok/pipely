package com.example.backend.jenkins.job.model;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Builder
@Getter
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    private JenkinsInfo jenkinsInfo;
}
