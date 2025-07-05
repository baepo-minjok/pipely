package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.Job;
import com.example.backend.jenkins.job.model.JobNotification;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JobNotificationRequestDto {
    private UUID jobId;
    private String name;
    private String eventType;
    private String channel;
    private String webhookUrl;
    private Boolean shouldNotify;

    public JobNotification toEntity(FreeStyle freeStyle, String credentialName) {
        return JobNotification.builder()
                .id(freeStyle.getId())
                .name(this.name)
                .createdAt(LocalDateTime.now())
                .shouldNotify(this.shouldNotify)
                .channel(this.channel)
                .webhookUrl(this.webhookUrl)
                .eventType(this.eventType)
                .credentialName(credentialName)
                .freeStyle(freeStyle)
                .build();
    }
}
