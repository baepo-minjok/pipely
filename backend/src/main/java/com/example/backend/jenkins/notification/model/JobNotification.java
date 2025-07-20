package com.example.backend.jenkins.notification.model;

import com.example.backend.jenkins.job.model.Pipeline;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_notification")
public class JobNotification {
    @Id
    @Column(name = "credential_name", nullable = false)
    private String credentialName;

    @Column(name = "pipeline_id")
    private UUID pipelineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Pipeline pipeline;

    @Column(name = "script_id", nullable = false)
    private UUID scriptId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "should_notify")
    private Boolean shouldNotify;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    private String webhookUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    public enum EventType {
        BUILD_SUCCESS,
        BUILD_FAIL
    }

    public enum Channel {
        SLACK,
        DISCORD
    }
}