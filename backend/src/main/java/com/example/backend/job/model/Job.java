package com.example.backend.job.model;

import com.example.backend.notification.model.JobNotification;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "job")
public class Job {

    @Id
    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "job_created_at")
    private LocalDateTime jobCreatedAt;

    private String description;

    private LocalDateTime deletedAt;

    private Boolean isDeleted;

    private Integer latestVersion;

    @Enumerated(EnumType.STRING)
    private BuildStatus buildStatus;

    public enum BuildStatus {
        SUCCESS, FAILED, ONPROGRESS
    }

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobSchedule> jobSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobVersion> jobVersions = new ArrayList<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobNotification> jobNotifications = new ArrayList<>();
}
