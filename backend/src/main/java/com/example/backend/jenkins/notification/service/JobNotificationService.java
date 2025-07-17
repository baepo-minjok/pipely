package com.example.backend.jenkins.notification.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
import com.example.backend.jenkins.job.repository.ScriptRepository;
import com.example.backend.jenkins.job.service.ConfigService;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.jenkins.notification.model.JobNotification;
import com.example.backend.jenkins.notification.model.dto.RequestDto;
import com.example.backend.jenkins.notification.model.dto.ResponseDto;
import com.example.backend.jenkins.notification.repository.JobNotificationRepository;
import com.example.backend.service.HttpClientService;
import com.example.backend.util.ScriptEditUtil;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobNotificationService {

    private final PipelineRepository pipelineRepository;
    private final PipelineVersionRepository pipelineVersionRepository;
    private final ApplicationEventPublisher publisher;
    private final JobNotificationRepository notificationRepository;
    private final JenkinsClientFactory jenkinsClient;
    private final ScriptRepository scriptRepository;
    private final ScriptEditUtil scriptEditUtil;
    private final ConfigService configService;
    private final HttpClientService httpClientService;
    private final MustacheFactory mf;
    private final PipelineService pipelineService;

    public static String removeInvalidXMLChars(String input) {
        StringBuilder out = new StringBuilder();
        for (char c : input.toCharArray()) {
            if ((c == 0x9) || (c == 0xA) || (c == 0xD) ||
                    ((c >= 0x20) && (c <= 0xD7FF)) ||
                    ((c >= 0xE000) && (c <= 0xFFFD)) ||
                    ((c >= 0x10000) && (c <= 0x10FFFF))) {
                out.append(c);
            }
        }
        return out.toString();
    }

    @Transactional
    public void createJobNotifications(List<RequestDto.createCredential> dtoList, UUID userId) {
        List<JobNotification> savedNotifications = new ArrayList<>();
        Set<UUID> affectedScriptIds = new HashSet<>();

        for (RequestDto.createCredential dto : dtoList) {
            UUID pipelineId = null;

            if (dto.getJobId() != null) {
                pipelineId = pipelineRepository.findById(dto.getJobId())
                        .map(Pipeline::getId)
                        .orElse(null);
            }

            Script script = scriptRepository.findById(dto.getScriptId())
                    .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND));

            affectedScriptIds.add(script.getId());

            String uuidSuffix = UUID.randomUUID().toString().substring(0, 8);
            String credentialName = String.format("%s_%s_%s_%s",
                    dto.getChannel().toUpperCase(),
                    script.getId().toString().substring(0, 8),
                    dto.getEventType().toUpperCase(),
                    uuidSuffix
            );

            JobNotification notification = dto.toEntity(pipelineId, credentialName);
            JobNotification saved = notificationRepository.save(notification);

            if (Boolean.TRUE.equals(dto.getShouldNotify())) {
                JenkinsClientFactory.JenkinsClient client = jenkinsClient.createClientForUser(userId);
                client.createGlobalCredential(credentialName, dto.getWebhookUrl()).block();
            }

            savedNotifications.add(saved);
        }

        for (UUID scriptId : affectedScriptIds) {
            updatePipelineConfigWithScript(scriptId);
        }
    }

    public List<ResponseDto.JobNotificationListResponseDto> getUserJobNotifications(UUID userId, UUID jobId) {
        List<JobNotification> notifications = notificationRepository
                .findByPipeline_JenkinsInfo_User_IdAndPipeline_Id(userId, jobId);

        return notifications.stream()
                .map(ResponseDto.JobNotificationListResponseDto::fromEntity)
                .toList();
    }

    public ResponseDto.JobNotificationDetailResponseDto getNotificationDetail(String credentialName) {
        JobNotification notification = notificationRepository.findByCredentialName(credentialName)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_NOTIFICATION_NOT_FOUND));

        return ResponseDto.JobNotificationDetailResponseDto.fromEntity(notification);
    }

    @Transactional
    public void updateNotification(RequestDto.JobNotificationUpdateRequestDto dto, UUID userId) {
        JobNotification oldEntity = notificationRepository.findByCredentialName(dto.getCredentialName())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_NOTIFICATION_NOT_FOUND));

        if (dto.getEventType() != null) oldEntity.setEventType(dto.getEventType());
        if (dto.getWebhookUrl() != null) oldEntity.setWebhookUrl(dto.getWebhookUrl());
        if (dto.getShouldNotify() != null) oldEntity.setShouldNotify(dto.getShouldNotify());

        notificationRepository.save(oldEntity);

        if (!Objects.equals(dto.getWebhookUrl(), oldEntity.getWebhookUrl())) {
            JenkinsClientFactory.JenkinsClient client = jenkinsClient.createClientForUser(userId);
            client.createGlobalCredential(oldEntity.getCredentialName(), oldEntity.getWebhookUrl()).block();
        }

        updatePipelineConfigWithScript(oldEntity.getScriptId());
    }

    @Transactional
    public void deleteNotification(RequestDto.JobNotificationDeleteRequestDto dto, UUID userId) {
        JobNotification notification = notificationRepository.findByCredentialName(dto.getCredentialName())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_NOTIFICATION_NOT_FOUND));

        UUID scriptId = notification.getScriptId();
        notificationRepository.delete(notification);

        // ✨ 인증 정보 포함된 클라이언트 생성
        JenkinsClientFactory.JenkinsClient client = jenkinsClient.createClientForUser(userId);
        client.deleteGlobalCredential(dto.getCredentialName()).block();

        updatePipelineConfigWithScript(scriptId);
    }

    private String createNotificationScript(String jobName, List<JobNotification> notifications) {
        Mustache mustache = mf.compile("template/notificationScript.mustache");

        List<Map<String, Object>> notificationList = notifications.stream().map(n -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("eventType", n.getEventType());
            entry.put("credentialId", n.getCredentialName());
            entry.put("webhookUrl", n.getWebhookUrl());
            entry.put("shouldNotify", Boolean.TRUE.equals(n.getShouldNotify()));
            entry.put("isSlack", "SLACK".equalsIgnoreCase(n.getChannel()));

            String eventType = Optional.ofNullable(n.getEventType()).orElse("").toUpperCase();
            entry.put("isBuildSuccess", eventType.equals("BUILD_SUCCESS"));
            entry.put("isBuildFail", eventType.equals("BUILD_FAIL"));

            return entry;
        }).collect(Collectors.toList());

        Map<String, Object> context = new HashMap<>();
        context.put("jobName", jobName != null ? jobName : "");
        context.put("notifications", notificationList);

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }

    private void updatePipelineConfigWithScript(UUID scriptId) {
        Script script = scriptRepository.findById(scriptId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND));

        List<JobNotification> notifications = notificationRepository.findAllByScriptIdAndShouldNotifyTrue(scriptId);

        String jobName = "";
        Optional<PipelineVersion> pipelineOpt = pipelineVersionRepository.findTopByScriptIdOrderByCreatedAtDesc(scriptId);
        if (pipelineOpt.isPresent()) {
            jobName = pipelineOpt.get().getPipeline().getName();
        }

        String newPostBlock = createNotificationScript(jobName, notifications);

        String updatedScript = replacePostBlock(script.getScript(), newPostBlock);
        script.setScript(updatedScript);
        scriptRepository.save(script);

        if (pipelineOpt.isEmpty()) {
            log.info("[JobNotificationService] 아직 pipeline이 존재하지 않음. scriptId: {}", scriptId);
            return;
        }

        Pipeline pipeline = pipelineOpt.get().getPipeline();

        PipelineVersion latestVersion = pipelineService.getLatestVersion(pipeline);

        String updatedConfigXml = updateScriptInConfigXml(latestVersion.getConfig(), updatedScript);

        pipeline.setUpdatedAt(LocalDateTime.now());
        Pipeline updatedPipeline = pipelineRepository.save(pipeline);

        PipelineVersion newVersion = PipelineVersion.builder()
                .pipeline(updatedPipeline)
                .createdAt(LocalDateTime.now())
                .config(updatedConfigXml)
                .script(script)
                .description(latestVersion.getDescription())
                .isTriggered(latestVersion.getIsTriggered())
                .schedule(latestVersion.getSchedule())
                .build();

        PipelineVersion saved = pipelineVersionRepository.save(newVersion);

        updateJenkinsServerConfig(updatedPipeline, newVersion);
    }

    private void updateJenkinsServerConfig(Pipeline pipeline, PipelineVersion newVersion) {
        JenkinsInfo info = pipeline.getJenkinsInfo();
        if (info == null) {
            throw new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND);
        }

        Script script = newVersion.getScript();
        if (script == null) {
            throw new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND);
        }

        Map<String, Object> context = new HashMap<>();

        context.put("description", newVersion.getDescription());
        context.put("trigger", newVersion.getIsTriggered());

        if (script != null) {
            String githubUrl = Optional.ofNullable(script.getGithubUrl()).orElse("");
            String rawScript = Optional.ofNullable(script.getScript()).orElse("");

            String injectedScript = scriptEditUtil.injectBooleanParams(rawScript);

            context.put("githubUrl", githubUrl);
            context.put("script", injectedScript);
        }

        String configXml = configService.createConfig(context);

        String jenkinsUrl = info.getUri() + "/job/" + pipeline.getName() + "/config.xml";
        HttpEntity<String> req = new HttpEntity<>(
                configXml,
                httpClientService.buildHeaders(
                        info,
                        new MediaType("application", "xml", StandardCharsets.UTF_8)
                )
        );
        publisher.publishEvent(new JobNotificationEvent<>(pipeline.getId(), jenkinsUrl, HttpMethod.POST, req, String.class));
    }

    private String replacePostBlock(String originalScript, String newPostBlock) {
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

    private String updateScriptInConfigXml(String originalXml, String newScript) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(removeInvalidXMLChars(originalXml))));

            NodeList scripts = doc.getElementsByTagName("script");
            Node scriptNode = (scripts.getLength() > 0) ? scripts.item(0) : doc.createElement("script");

            CDATASection cdata = doc.createCDATASection(newScript);
            scriptNode.setTextContent("");
            scriptNode.appendChild(cdata);

            if (scripts.getLength() == 0) doc.getDocumentElement().appendChild(scriptNode);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();

        } catch (Exception e) {
            log.error("Failed to update script in config.xml", e);
            throw new CustomException(ErrorCode.JENKINS_XML_UPDATE_FAIL);
        }
    }
}