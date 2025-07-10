package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.job.model.JobNotification;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
public class JobNotificationResponseDto {

    @Builder
    @Data
    public static class JobNotificationListResponseDto {
        private UUID jobId;
        private String jobName;
        private String eventType;
        private String credentialName;
        private String webhookUrl;
        private Boolean shouldNotify;

        public static JobNotificationListResponseDto fromEntity(JobNotification notification) {
            return JobNotificationListResponseDto.builder()
                    .jobId(notification.getPipeline().getId())
                    .jobName(notification.getPipeline().getJobName())
                    .eventType(notification.getEventType())
                    .credentialName(notification.getCredentialName())
                    .webhookUrl(notification.getWebhookUrl())
                    .shouldNotify(notification.getShouldNotify())
                    .build();
        }
    }
}
