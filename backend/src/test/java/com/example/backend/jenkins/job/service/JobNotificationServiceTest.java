package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.notification.model.JobNotification;
import com.example.backend.jenkins.notification.repository.JobNotificationRepository;
import com.example.backend.jenkins.notification.service.JobNotificationService;
import com.example.backend.service.HttpClientService;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;

import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobNotificationServiceTest {

    @Mock private JobNotificationRepository notificationRepository;
    @Mock private com.example.backend.jenkins.job.repository.ScriptRepository scriptRepository;
    @Mock private HttpClientService httpClientService;
    @Mock private MustacheFactory mustacheFactory;
    @Mock private Mustache mustache;

    @InjectMocks private JobNotificationService jobNotificationService;

    private JenkinsInfo info;
    private Script script;
    private UUID scriptId;
    private RequestDto.NotificationDto dto;

    @BeforeEach
    void setup() {
        scriptId = UUID.randomUUID();
        info = JenkinsInfo.builder().id(UUID.randomUUID()).uri("http://jenkins").build();
        script = Script.builder().id(scriptId).script("pipeline {}").build();

        dto = RequestDto.NotificationDto.builder()
                .channel(JobNotification.Channel.SLACK)
                .eventType(JobNotification.EventType.BUILD_FAIL)
                .webhookUrl("http://webhook")
                .shouldNotify(true)
                .credentialName("cred123")
                .name("빌드 실패 알림")
                .build();
    }

    /* -------------------- createJobNotifications -------------------- */

    @Test
    @DisplayName("createJobNotifications: scriptId가 DB에 없으면 예외 발생")
    void createJobNotifications_scriptNotFound() {
        when(scriptRepository.findById(scriptId)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () ->
                jobNotificationService.createJobNotifications(List.of(dto), info, scriptId));
    }

    @Test
    @DisplayName("createJobNotifications: 정상 저장 및 Credential 생성 호출")
    void createJobNotifications_success() {
        when(scriptRepository.findById(scriptId)).thenReturn(Optional.of(script));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(httpClientService.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(null);

        List<JobNotification> result = jobNotificationService.createJobNotifications(List.of(dto), info, scriptId);

        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).save(any());
        verify(httpClientService, times(1)).exchange(any(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    /* -------------------- syncJobNotifications -------------------- */

    @Test
    @DisplayName("syncJobNotifications: 신규 알림 생성 시 Credential 생성 및 저장")
    void syncJobNotifications_createNew() {
        when(notificationRepository.findByScriptId(scriptId)).thenReturn(Collections.emptyList());
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(httpClientService.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(null);

        List<JobNotification> result = jobNotificationService.syncJobNotifications(List.of(dto), info, script);

        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("syncJobNotifications: 기존 알림이 변경되면 updateCredential 호출 및 저장")
    void syncJobNotifications_updateExisting() {
        JobNotification existing = JobNotification.builder()
                .credentialName("cred123").webhookUrl("old").shouldNotify(false).name("old").script(script).build();

        when(notificationRepository.findByScriptId(scriptId)).thenReturn(List.of(existing));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(httpClientService.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(null);

        List<JobNotification> result = jobNotificationService.syncJobNotifications(List.of(dto), info, script);

        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("syncJobNotifications: 기존 알림이 incomingList에 없으면 Credential 삭제 및 DB 삭제")
    void syncJobNotifications_deleteObsolete() {
        JobNotification existing = JobNotification.builder()
                .credentialName("oldCred").webhookUrl("url").shouldNotify(true).name("old").script(script).build();

        when(notificationRepository.findByScriptId(scriptId)).thenReturn(List.of(existing));
        when(httpClientService.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(null);

        jobNotificationService.syncJobNotifications(Collections.emptyList(), info, script);

        verify(notificationRepository, times(1)).delete(existing);
        verify(httpClientService, times(1)).exchange(contains("/doDelete"), eq(HttpMethod.POST), any(), eq(String.class));
    }

    /* -------------------- createNotificationScript -------------------- */

    @Test
    @DisplayName("createNotificationScript: 알림 리스트를 Mustache 템플릿으로 변환")
    void createNotificationScript_success() {
        when(mustacheFactory.compile("template/notificationScript.mustache")).thenReturn(mustache);
        doAnswer(inv -> {
            StringWriter w = inv.getArgument(0, StringWriter.class);
            w.write("generatedScript");
            return w;
        }).when(mustache).execute(any(StringWriter.class), ArgumentMatchers.<Map<String, Object>>any());

        JobNotification notification = JobNotification.builder()
                .channel(JobNotification.Channel.SLACK)
                .eventType(JobNotification.EventType.BUILD_FAIL)
                .webhookUrl("url")
                .shouldNotify(true)
                .credentialName("cred")
                .build();

        String result = jobNotificationService.createNotificationScript(List.of(notification));
        assertEquals("generatedScript", result);
    }

    /* -------------------- replacePostBlock -------------------- */

    @Test
    @DisplayName("replacePostBlock: post 블럭 없으면 새 블럭 추가")
    void replacePostBlock_noPostBlock() {
        String original = "pipeline {}";
        String result = jobNotificationService.replacePostBlock(original, "newPost");
        assertTrue(result.contains("newPost"));
    }

    @Test
    @DisplayName("replacePostBlock: post 블럭 교체 성공")
    void replacePostBlock_existingPostBlock() {
        String original = "pipeline { post { } }";
        String result = jobNotificationService.replacePostBlock(original, "newPost");
        assertTrue(result.contains("newPost"));
    }

    @Test
    @DisplayName("replacePostBlock: post 블럭 닫히지 않으면 예외 발생")
    void replacePostBlock_unclosedPostBlock() {
        String original = "pipeline { post { ";
        assertThrows(IllegalStateException.class, () ->
                jobNotificationService.replacePostBlock(original, "newPost"));
    }

    /* -------------------- Credential 관련 메서드 -------------------- */

    @Test
    @DisplayName("createCredential: Jenkins에 POST 요청 호출")
    void createCredential_success() {
        when(httpClientService.buildHeaders(any(), any())).thenReturn(new HttpHeaders());
        when(httpClientService.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(null);

        assertDoesNotThrow(() -> jobNotificationService.createCredential(info, "cred", "secret"));
        verify(httpClientService, times(1)).exchange(contains("/createCredentials"), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    @DisplayName("updateCredential: 기존 Credential 삭제 후 새로 생성")
    void updateCredential_success() {
        when(httpClientService.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(null);

        jobNotificationService.updateCredential(info, "cred", "secret");
        verify(httpClientService, atLeast(2)).exchange(any(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    @DisplayName("deleteCredential: Jenkins에 Credential 삭제 요청")
    void deleteCredential_success() {
        when(httpClientService.buildHeaders(any(), any())).thenReturn(new HttpHeaders());
        when(httpClientService.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(null);

        jobNotificationService.deleteCredential(info, "cred");
        verify(httpClientService, times(1)).exchange(contains("/doDelete"), eq(HttpMethod.POST), any(), eq(String.class));
    }

    /* -------------------- getEnabledNotifications -------------------- */

    @Test
    @DisplayName("getEnabledNotifications: shouldNotify=true인 알림만 조회")
    void getEnabledNotifications_success() {
        when(notificationRepository.findByScriptIdAndShouldNotifyTrue(scriptId))
                .thenReturn(List.of(JobNotification.builder().script(script).shouldNotify(true).build()));

        List<JobNotification> result = jobNotificationService.getEnabledNotifications(script);
        assertEquals(1, result.size());
    }
}