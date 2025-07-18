package com.example.backend.jenkins.notification.model.dto;

import com.example.backend.jenkins.notification.model.JobNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
public class ResponseDto {

    @Builder
    @Data
    @Schema(name = "JobNotificationListResponseDto", description = "Job 알림 목록 응답 DTO")
    public static class JobNotificationListResponseDto {

        @Schema(description = "Job ID (Pipeline UUID)", example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
        private UUID jobId;

        @Schema(description = "Job 이름", example = "example-job")
        private String jobName;

        @Schema(description = "알림 이벤트 타입", example = "BUILD_FAIL")
        private String eventType;

        @Schema(description = "알림 Credential 이름", example = "DISCORD_1472d5da_BUILD_SUCCESS_5850a9c6")
        private String credentialName;

        @Schema(description = "Webhook URL", example = "https://discord.com/api/webhooks/...")
        private String webhookUrl;

        @Schema(description = "알림 전송 여부", example = "true")
        private Boolean shouldNotify;

        public static JobNotificationListResponseDto fromEntity(JobNotification notification) {
            return JobNotificationListResponseDto.builder()
                    .jobId(notification.getPipeline().getId())
                    .jobName(notification.getPipeline().getName())
                    .eventType(notification.getEventType())
                    .credentialName(notification.getCredentialName())
                    .webhookUrl(notification.getWebhookUrl())
                    .shouldNotify(notification.getShouldNotify())
                    .build();
        }
    }

    @Builder
    @Data
    @Schema(name = "JobNotificationDetailResponseDto", description = "Job 알림 상세 응답 DTO")
    public static class JobNotificationDetailResponseDto {

        @Schema(description = "Job ID (Pipeline UUID)", example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
        private UUID jobId;

        @Schema(description = "Job 이름", example = "example-job")
        private String jobName;

        @Schema(description = "알림 이벤트 타입", example = "BUILD_SUCCESS")
        private String eventType;

        @Schema(description = "알림 Credential 이름", example = "DISCORD_1472d5da_BUILD_SUCCESS_5850a9c6")
        private String credentialName;

        @Schema(description = "Webhook URL", example = "https://discord.com/api/webhooks/...")
        private String webhookUrl;

        @Schema(description = "알림 전송 여부", example = "true")
        private Boolean shouldNotify;

        public static JobNotificationDetailResponseDto fromEntity(JobNotification notification) {
            return JobNotificationDetailResponseDto.builder()
                    .jobId(notification.getPipeline().getId())
                    .jobName(notification.getPipeline().getName())
                    .eventType(notification.getEventType())
                    .credentialName(notification.getCredentialName())
                    .webhookUrl(notification.getWebhookUrl())
                    .shouldNotify(notification.getShouldNotify())
                    .build();
        }
    }
}
