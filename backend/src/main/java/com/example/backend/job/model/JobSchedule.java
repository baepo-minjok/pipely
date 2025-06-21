package com.example.backend.job.model;

import jakarta.persistence.*;

import java.io.Serializable;
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
            @JoinColumn(name = "name", referencedColumnName = "name", insertable=false, updatable=false),
            @JoinColumn(name = "job_created_at", referencedColumnName = "job_created_at", insertable=false, updatable=false)
    })
    private Job job;
}


