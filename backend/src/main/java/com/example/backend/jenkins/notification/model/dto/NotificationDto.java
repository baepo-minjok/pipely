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
@Schema(
        name = "NotificationDto",
        description = """
                  Job 알림 채널 설정 DTO.
                  - key는 채널명(slack, discord 등), 값은 알림 설정
                  - checkSuccess: 빌드 성공 시 알림 보낼지
                  - checkFailure: 빌드 실패 시 알림 보낼지
                  - webhookUrl: 채널이 Webhook을 사용할 때만 필요(예: Discord), Slack App 사용 시 토큰 기반이면 생략 가능
                """
)
public class NotificationDto {
    @Schema(
            description = "Webhook URL (Discord 등 Webhook형 채널에 사용)",
            example = "https://discord.com/api/webhooks/xxxxx/yyyyy",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String webhookUrl;

    @Schema(
            description = "빌드 성공 이벤트 알림 여부",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean checkSuccess;

    @Schema(
            description = "빌드 실패 이벤트 알림 여부",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
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
