package com.example.backend.jenkins.job.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@IdClass(JobScheduleId.class)
@Table(name = "job_schedule")
public class JobSchedule {

    @Id
    @Column(name = "created_by")
    private String createdBy;

    @Id
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "name")
    private String name;

    @Column(name = "job_created_at")
    private LocalDateTime jobCreatedAt;

    private Boolean isEnabled;

    private String cronExpr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "created_at", referencedColumnName = "created_at", insertable = false, updatable = false)
    })
    private Job job;
}


