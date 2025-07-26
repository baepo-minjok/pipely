package com.example.backend.jenkins.notification.model;

import com.example.backend.jenkins.job.model.PipelineVersion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

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
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "알림 고유 UUID",
            example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a"
    )
    private UUID id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_version_id", nullable = false, updatable = false)
    private PipelineVersion pipelineVersion;

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