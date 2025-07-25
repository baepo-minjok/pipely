package com.example.backend.jenkins.notification.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.ScriptRepository;
import com.example.backend.jenkins.notification.model.JobNotification;
import com.example.backend.jenkins.notification.repository.JobNotificationRepository;
import com.example.backend.service.HttpClientService;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobNotificationService {

    private final JobNotificationRepository notificationRepository;
    private final ScriptRepository scriptRepository;
    private final HttpClientService httpClientService;
    private final MustacheFactory mf;


    @Transactional
    public List<JobNotification> createJobNotifications(List<RequestDto.NotificationDto> dtoList, JenkinsInfo info, UUID scriptId) {
        List<JobNotification> savedNotifications = new ArrayList<>();

        for (RequestDto.NotificationDto dto : dtoList) {
            Script script = scriptRepository.findById(scriptId)
                    .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND));

            String credentialName = generateCredentialName(script.getId(), dto.getChannel(), dto.getEventType());

            JobNotification notification = dto.toEntity(credentialName, scriptId);
            JobNotification saved = notificationRepository.save(notification);
            savedNotifications.add(saved);

            createCredential(info, credentialName, dto.getWebhookUrl());
        }

        return savedNotifications;
    }

    @Transactional
    public List<JobNotification> syncJobNotifications(List<RequestDto.NotificationDto> incomingList, JenkinsInfo info, Script script) {
        UUID scriptId = script.getId();

        List<JobNotification> existingList = notificationRepository.findByScriptId(scriptId);

        Map<String, JobNotification> existingMap = existingList.stream()
                .collect(Collectors.toMap(JobNotification::getCredentialName, n -> n));

        List<JobNotification> result = new ArrayList<>();

        for (RequestDto.NotificationDto dto : incomingList) {
            String credentialName = dto.getCredentialName();

            JobNotification existing = credentialName != null
                    ? existingMap.remove(credentialName)
                    : null;

            if (existing == null) {
                String newCredentialName = credentialName != null
                        ? credentialName
                        : generateCredentialName(scriptId, dto.getChannel(), dto.getEventType());

                createCredential(info, newCredentialName, dto.getWebhookUrl());

                JobNotification created = dto.toEntity(newCredentialName, scriptId);
                result.add(notificationRepository.save(created));
            } else {
                boolean changed = false;

                if (!Objects.equals(existing.getWebhookUrl(), dto.getWebhookUrl())) {
                    updateCredential(info, credentialName, dto.getWebhookUrl());
                    existing.setWebhookUrl(dto.getWebhookUrl());
                    changed = true;
                }

                if (!Objects.equals(existing.getShouldNotify(), dto.getShouldNotify())) {
                    existing.setShouldNotify(dto.getShouldNotify());
                    changed = true;
                }

                if (!Objects.equals(existing.getName(), dto.getName())) {
                    existing.setName(dto.getName());
                    changed = true;
                }

                if (changed) {
                    notificationRepository.save(existing);
                }

                result.add(existing);
            }
        }

        for (JobNotification toDelete : existingMap.values()) {
            deleteCredential(info, toDelete.getCredentialName());
            notificationRepository.delete(toDelete);
        }

        return result;
    }

    public String createNotificationScript(List<JobNotification> notifications) {
        Mustache mustache = mf.compile("template/notificationScript.mustache");

        List<Map<String, Object>> notificationList = notifications.stream().map(n -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("eventType", n.getEventType());
            entry.put("credentialId", n.getCredentialName());
            entry.put("webhookUrl", n.getWebhookUrl());
            entry.put("shouldNotify", Boolean.TRUE.equals(n.getShouldNotify()));
            entry.put("isSlack", n.getChannel() == JobNotification.Channel.SLACK);
            entry.put("isBuildSuccess", n.getEventType() == JobNotification.EventType.BUILD_SUCCESS);
            entry.put("isBuildFail", n.getEventType() == JobNotification.EventType.BUILD_FAIL);
            return entry;
        }).collect(Collectors.toList());

        Map<String, Object> context = new HashMap<>();
        context.put("notifications", notificationList);

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }

    public String replacePostBlock(String originalScript, String newPostBlock) {
        int postIndex = originalScript.indexOf("post {");
        if (postIndex == -1) {
            return originalScript.trim() + "\n\n" + newPostBlock;
        }

        int braceCount = 0;
        boolean started = false;
        int endIndex = -1;

        for (int i = postIndex; i < originalScript.length(); i++) {
            char c = originalScript.charAt(i);

            if (c == '{') {
                braceCount++;
                started = true;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && started) {
                    endIndex = i;
                    break;
                }
            }
        }

        if (endIndex == -1) {
            throw new IllegalStateException("post 블럭의 끝을 찾을 수 없습니다.");
        }

        String beforePost = originalScript.substring(0, postIndex).trim();
        String afterPost = originalScript.substring(endIndex + 1).trim();
        return beforePost + "\n\n" + newPostBlock + "\n\n" + afterPost;
    }

    private String generateCredentialName(UUID scriptId, JobNotification.Channel channel, JobNotification.EventType eventType) {
        String uuidSuffix = UUID.randomUUID().toString().substring(0, 8);
        return String.format(
                "%s_%s_%s_%s",
                channel.name(),
                scriptId.toString().substring(0, 8),
                eventType.name(),
                uuidSuffix
        );
    }

    public void createCredential(JenkinsInfo info, String credentialId, String secret) {
        String url = info.getUri().replaceAll("/+$", "")
                + "/credentials/store/system/domain/_/createCredentials";

        String jsonPayload = "{\n" +
                "  \"\": \"0\",\n" +
                "  \"credentials\": {\n" +
                "    \"scope\": \"GLOBAL\",\n" +
                "    \"id\": \"" + credentialId + "\",\n" +
                "    \"description\": \"Webhook for pipeline\",\n" +
                "    \"secret\": \"" + secret + "\",\n" +
                "    \"$class\": \"org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl\"\n" +
                "  }\n" +
                "}";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("json=" + jsonPayload, headers);

        httpClientService.exchange(url, HttpMethod.POST, request, String.class);
    }

    public void updateCredential(JenkinsInfo info, String credentialId, String newSecret) {
        deleteCredential(info, credentialId);
        createCredential(info, credentialId, newSecret);
    }

    public void deleteCredential(JenkinsInfo info, String credentialId) {
        String url = info.getUri().replaceAll("/+$", "")
                + "/credentials/store/system/domain/_/credential/" + credentialId + "/doDelete";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("Submit=OK", headers);

        httpClientService.exchange(url, HttpMethod.POST, request, String.class);
    }

    public Script updateScriptWithJobNotifications(Script script, List<JobNotification> notifications) {
        String newPostBlock = createNotificationScript(notifications);
        String updatedScript = replacePostBlock(script.getScript(), newPostBlock);
        script.setScript(updatedScript);
        return scriptRepository.save(script);
    }

    public List<JobNotification> getEnabledNotifications(Script script) {
        return notificationRepository.findByScriptIdAndShouldNotifyTrue(script.getId());
    }
}