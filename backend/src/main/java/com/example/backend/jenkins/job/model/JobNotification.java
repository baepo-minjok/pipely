package com.example.backend.jenkins.job.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(JobNotificationId.class)
@Table(name = "job_notification")
public class JobNotification {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Id
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "should_notify")
    private Boolean shouldNotify;

    private String channel;

    private String webhookUrl;

    private String eventType;

    @Column(name = "credential_name")
    private String credentialName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false),
    })
    private FreeStyle freeStyle;
}

