package com.example.backend.job.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@IdClass(JobErrorLogId.class)
@Table(name = "job_error_log")
public class JobErrorLog {

    @Id
    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Id
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Integer version;

    @Column(name = "version_created_at")
    private LocalDateTime versionCreatedAt;


    private String errorType;

    @Lob
    private String errorMessage;

    @Lob
    private String fullLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "version", referencedColumnName = "version", insertable=false, updatable=false),
            @JoinColumn(name = "version_created_at", referencedColumnName = "version_created_at", insertable=false, updatable=false),
    })
    private JobVersion jobVersion;
}


