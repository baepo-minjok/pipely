package com.example.backend.jenkins.notification.model.dto;

import com.example.backend.jenkins.notification.model.JobNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class RequestDto {

    @Data
    @Schema(name = "CreateCredentialDto", description = "Job 알림 생성 요청 DTO")
    public static class createCredential {

        @Schema(description = "Job ID (Pipeline UUID)", example = "d5b77f8e-934b-4b20-afe2-9c30d3329629")
        private UUID jobId;

        @Schema(description = "Script ID (연결된 스크립트 UUID)", example = "c1e2b456-1234-4bcd-a0ef-56789abcdef0")
        private UUID scriptId;

        @Schema(description = "알림 이름", example = "DISCORD_1472d5da_BUILD_SUCCESS_5850a9c6")
        private String name;

        @Schema(description = "이벤트 유형", example = "BUILD_SUCCESS")
        private String eventType;

        @Schema(description = "알림 채널", example = "DISCORD")
        private String channel;

        @Schema(description = "Webhook URL", example = "https://discord.com/api/webhooks/...")
        private String webhookUrl;

        @Schema(description = "알림 여부", example = "true")
        private Boolean shouldNotify;

        public JobNotification toEntity(UUID pipelineId, String credentialName) {
            return JobNotification.builder()
                    .pipelineId(pipelineId)
                    .scriptId(this.scriptId)
                    .name(this.name)
                    .createdAt(LocalDateTime.now())
                    .shouldNotify(this.shouldNotify)
                    .channel(this.channel)
                    .webhookUrl(this.webhookUrl)
                    .eventType(this.eventType)
                    .credentialName(credentialName)
                    .build();
        }
    }

    @Data
    @Schema(name = "SendJobNotificationRequestDto", description = "Job 알림 전송 요청 DTO")
    public static class SendJobNotificationRequestDto {

        @Schema(description = "Job ID (Pipeline UUID)", example = "a1a1a1a1-b2b2-4c4c-d5d5-e6e6e6e6e6e6")
        private UUID jobId;
    }

    @Data
    @Schema(name = "NotificationListRequestDto", description = "Job 알림 목록 조회 요청 DTO")
    public static class NotificationListRequestDto {

        @Schema(description = "Job ID (Pipeline UUID)", example = "b7c7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
        private UUID jobId;
    }

    @Data
    @Schema(name = "NotificationDetailRequestDto", description = "Job 알림 상세 조회 요청 DTO")
    public static class NotificationDetailRequestDto {

        @Schema(description = "Credential 이름", example = "DISCORD_1472d5da_BUILD_SUCCESS_5850a9c6")
        private String credentialName;
    }

    @Data
    @Schema(name = "JobNotificationUpdateRequestDto", description = "Job 알림 수정 요청 DTO")
    public static class JobNotificationUpdateRequestDto {

        @Schema(description = "Credential 이름", example = "DISCORD_1472d5da_BUILD_SUCCESS_5850a9c6")
        private String credentialName;

        @Schema(description = "새 이벤트 유형", example = "BUILD_SUCCESS")
        private String eventType;

        @Schema(description = "새 Webhook URL", example = "https://discord.com/api/webhooks/...")
        private String webhookUrl;

        @Schema(description = "알림 여부", example = "false")
        private Boolean shouldNotify;

        public JobNotification toEntity(JobNotification notification) {
            return JobNotification.builder()
                    .pipelineId(notification.getPipelineId())
                    .name(notification.getName())
                    .createdAt(notification.getCreatedAt())
                    .pipeline(notification.getPipeline())
                    .credentialName(notification.getCredentialName())
                    .eventType(this.eventType != null ? this.eventType : notification.getEventType())
                    .webhookUrl(this.webhookUrl != null ? this.webhookUrl : notification.getWebhookUrl())
                    .shouldNotify(this.shouldNotify != null ? this.shouldNotify : notification.getShouldNotify())
                    .channel(notification.getChannel())
                    .build();
        }
    }

    @Data
    @Schema(name = "JobNotificationDeleteRequestDto", description = "Job 알림 삭제 요청 DTO")
    public static class JobNotificationDeleteRequestDto {

        @Schema(description = "Credential 이름", example = "DISCORD_1472d5da_BUILD_SUCCESS_5850a9c6")
        private String credentialName;
    }
}
