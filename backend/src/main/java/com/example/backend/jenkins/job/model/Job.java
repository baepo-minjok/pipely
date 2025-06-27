package com.example.backend.jenkins.job.model;

import com.example.backend.notification.model.JobNotification;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "job")
public class Job {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String description;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private Integer latestVersion;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobSchedule> jobSchedules = new ArrayList<>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobVersion> jobVersions = new ArrayList<>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobNotification> jobNotifications = new ArrayList<>();

    public enum JobType {
        FREESTYLE, PIPELINE
    }
}
