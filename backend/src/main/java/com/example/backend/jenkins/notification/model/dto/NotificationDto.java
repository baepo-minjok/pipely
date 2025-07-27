package com.example.backend.jenkins.notification.model.dto;

import com.example.backend.jenkins.notification.model.JobNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "NotificationDto", description = "Job 알림 생성/수정 DTO")
public class NotificationDto {
    @Schema(description = "Webhook URL", example = "https://discord.com/api/webhooks/...")
    private String webhookUrl;

    private boolean checkSuccess;

    private boolean checkFailure;

    public static Map<String, NotificationDto> toNotificationMap(List<JobNotification> jobNotifications) {
        Map<String, NotificationDto> result = new HashMap<>();

        for (JobNotification jn : jobNotifications) {
            String key = jn.getChannel().name().toLowerCase(); // slack, discord

            // 채널별로 하나의 NotificationDto만 유지
            NotificationDto dto = result.getOrDefault(key,
                    NotificationDto.builder()
                            .webhookUrl(jn.getWebhookUrl())
                            .checkSuccess(false)
                            .checkFailure(false)
                            .build()
            );

            // EventType에 따라 플래그 세팅
            if (jn.getEventType() == JobNotification.EventType.BUILD_SUCCESS) {
                dto.setCheckSuccess(true);
            } else if (jn.getEventType() == JobNotification.EventType.BUILD_FAIL) {
                dto.setCheckFailure(true);
            }

            result.put(key, dto);
        }
        return result;
    }

}
