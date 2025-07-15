package com.example.backend.jenkins.notification.model.dto;

import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.notification.model.JobNotification;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RequestDto {

    @Data
    public static class createCredential{
        private UUID jobId;
        private UUID scriptId;
        private String name;
        private String eventType;
        private String channel;
        private String webhookUrl;
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
    public static class SendJobNotificationRequestDto {
        private UUID jobId;
    }

    @Data
    public static class NotificationListRequestDto {
        private UUID jobId;
    }

    @Data
    public static class NotificationDetailRequestDto {
        private String credentialName;
    }

    @Data
    public static class JobNotificationUpdateRequestDto {
        private String credentialName;
        private String eventType;
        private String webhookUrl;
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
    public static class JobNotificationDeleteRequestDto {
        private String credentialName;
    }
}
