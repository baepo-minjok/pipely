package com.example.backend.jenkins.job.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@IdClass(JobVersionId.class)
@Table(name = "job_version")
public class JobVersion {

    @Id
    private Integer version;

    @Id
    @Column(name = "version_created_at")
    private LocalDateTime versionCreatedAt;

    @Column(name = "name")
    private String name;

    @Column(name = "job_created_at")
    private LocalDateTime jobCreatedAt;

    @Lob
    private String scriptContent;

    private String changeLog;

    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "created_at", referencedColumnName = "created_at", insertable = false, updatable = false)
    })
    private Job job;

    @OneToMany(mappedBy = "jobVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobErrorLog> errorLogs = new ArrayList<>();
}