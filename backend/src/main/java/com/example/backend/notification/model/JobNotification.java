package com.example.backend.notification.model;

import com.example.backend.jenkins.job.model.Job;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@IdClass(JobNotificationId.class)
@Table(name = "job_notification")
public class JobNotification {

    @Id
    @Column(name = "name")
    private String name;


    @Id
    @Column(name = "job_created_at")
    private LocalDateTime jobCreatedAt;

    private Boolean shouldNotify;

    private String recipient;

    private String channel;

    private String webhookUrl;

    private String eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "name", referencedColumnName = "name", insertable = false, updatable = false),
            @JoinColumn(name = "job_created_at", referencedColumnName = "job_created_at", insertable = false, updatable = false)
    })
    private Job job;
}

