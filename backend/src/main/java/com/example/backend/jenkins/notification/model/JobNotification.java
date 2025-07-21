package com.example.backend.jenkins.notification.model;

import com.example.backend.jenkins.job.model.Pipeline;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "JobNotification",
        description = "Jenkins 파이프라인 빌드 알림 정보를 나타내는 엔티티입니다. " +
                "알림을 보낼 채널, 이벤트 종류, 웹훅 주소 등을 포함합니다."
)
public class JobNotification {

    @Id
    @Column(name = "credential_name", nullable = false)
    @Schema(description = "알림 이름 (Primary Key)", example = "DISCORD_1472d5da_BUILD_SUCCESS_5850a9c6", requiredMode = Schema.RequiredMode.REQUIRED)
    private String credentialName;

    @Column(name = "pipeline_id")
    @Schema(description = "연결된 파이프라인 ID (Pipeline Entity의 UUID)", example = "4fd7a1a1-c209-44a2-b18e-b3e8c73eeb82", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID pipelineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Schema(description = "연결된 Pipeline 엔티티 객체 (읽기 전용)", hidden = true)
    private Pipeline pipeline;

    @Column(name = "script_id", nullable = false)
    @Schema(description = "스크립트 식별자 (Script UUID)", example = "2de67452-b4aa-46b0-9e3d-523db27c43c4", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID scriptId;

    @Column(name = "name", nullable = false)
    @Schema(description = "알림 이름", example = "Slack 빌드 성공 알림", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "알림 등록 시간", example = "2025-07-20T15:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;

    @Column(name = "should_notify")
    @Schema(description = "해당 알림을 보낼지 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean shouldNotify;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    @Schema(description = "알림 채널 종류", example = "SLACK", requiredMode = Schema.RequiredMode.REQUIRED)
    private Channel channel;

    @Column(name = "webhookUrl")
    @Schema(description = "알림 전송에 사용되는 Webhook URL", example = "https://hooks.slack.com/services/xxx/yyy/zzz", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String webhookUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    @Schema(description = "알림을 보낼 빌드 이벤트 종류", example = "BUILD_SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED)
    private EventType eventType;

    public enum EventType {
        @Schema(description = "빌드 성공 이벤트")
        BUILD_SUCCESS,

        @Schema(description = "빌드 실패 이벤트")
        BUILD_FAIL
    }

    public enum Channel {
        @Schema(description = "Slack 알림 채널")
        SLACK,

        @Schema(description = "Discord 알림 채널")
        DISCORD
    }
}