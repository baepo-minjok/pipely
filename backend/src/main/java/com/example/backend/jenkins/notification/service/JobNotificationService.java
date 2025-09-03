package com.example.backend.jenkins.notification.service;

import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.notification.model.JobNotification;
import com.example.backend.jenkins.notification.model.dto.NotificationDto;
import com.example.backend.jenkins.notification.repository.JobNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobNotificationService {

    private final JobNotificationRepository notificationRepository;

    /**
     * Converts the NotificationMap into a list of JobNotification entities and saves them.
     *
     * @param notificationMap key=channel name (slack/discord), value=NotificationDto
     * @param pipelineVersion The pipeline version entity to which the notification will be linked.
     */
    @Transactional
    public void saveJobNotifications(Map<String, NotificationDto> notificationMap,
                                     PipelineVersion pipelineVersion) {

        if (notificationMap == null || notificationMap.isEmpty()) {
            return;
        }

        List<JobNotification> entities = new ArrayList<>();

        for (Map.Entry<String, NotificationDto> entry : notificationMap.entrySet()) {
            String key = entry.getKey();
            NotificationDto dto = entry.getValue();

            if (dto.isCheckSuccess()) {
                entities.add(
                        JobNotification.builder()
                                .channel(parseChannel(key))
                                .eventType(JobNotification.EventType.BUILD_SUCCESS)
                                .webhookUrl(dto.getWebhookUrl())
                                .pipelineVersion(pipelineVersion)
                                .build()
                );
            }

            if (dto.isCheckFailure()) {
                entities.add(
                        JobNotification.builder()
                                .channel(parseChannel(key))
                                .eventType(JobNotification.EventType.BUILD_FAIL)
                                .webhookUrl(dto.getWebhookUrl())
                                .pipelineVersion(pipelineVersion)
                                .build()
                );
            }
        }
        // 저장
        List<JobNotification> saved = notificationRepository.saveAll(entities);

        pipelineVersion.getJobNotificationList().addAll(saved);
    }

    /**
     * Delete existing notification information and update it by saving a new one.
     *
     * @param notificationMap: New notification settings
     * @param pipelineVersion: The pipeline version entity to which the notification will be linked
     */
    @Transactional
    public void updateJobNotifications(Map<String, NotificationDto> notificationMap,
                                       PipelineVersion pipelineVersion) {
        pipelineVersion.getJobNotificationList().clear();
        notificationRepository.deleteJobNotificationByPipelineVersion(pipelineVersion);
        saveJobNotifications(notificationMap, pipelineVersion);
    }

    /**
     * Convert key(discord/slack) to JobNotification.Channel enum
     *
     * @param key: Channel key (slack/discord)
     * @return JobNotification.Channel enum value
     */
    private JobNotification.Channel parseChannel(String key) {
        if (key == null) {
            throw new IllegalArgumentException("알림 채널 키가 null입니다.");
        }
        return switch (key.toLowerCase()) {
            case "slack" -> JobNotification.Channel.SLACK;
            case "discord" -> JobNotification.Channel.DISCORD;
            default -> throw new IllegalArgumentException("지원하지 않는 채널: " + key);
        };
    }

}