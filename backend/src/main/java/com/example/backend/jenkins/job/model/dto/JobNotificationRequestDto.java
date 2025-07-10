package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.Job;
import com.example.backend.jenkins.job.model.JobNotification;
import com.example.backend.jenkins.job.model.pipeline.Pipeline;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JobNotificationRequestDto {

    @Data
    public static class createCredential{
        private UUID jobId;
        private String name;
        private String eventType;
        private String channel;
        private String webhookUrl;
        private Boolean shouldNotify;

        public JobNotification toEntity(Pipeline pipeline, String credentialName) {
            return JobNotification.builder()
                    .id(pipeline.getId())
                    .name(this.name)
                    .createdAt(LocalDateTime.now())
                    .shouldNotify(this.shouldNotify)
                    .channel(this.channel)
                    .webhookUrl(this.webhookUrl)
                    .eventType(this.eventType)
                    .credentialName(credentialName)
                    .pipeline(pipeline)
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
}
