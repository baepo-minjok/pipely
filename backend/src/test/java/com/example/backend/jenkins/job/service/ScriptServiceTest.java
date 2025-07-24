package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.ScriptRepository;
import com.example.backend.jenkins.notification.service.JobNotificationService;
import com.example.backend.util.ScriptEditUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ScriptServiceTest {

    @Mock private ScriptRepository scriptRepository;
    @Mock private ConfigService configService;
    @Mock private JenkinsInfoService jenkinsInfoService;
    @Mock private JobNotificationService jobNotificationService;
    @Mock private ScriptEditUtil scriptEditUtil;

    @InjectMocks private ScriptService scriptService;

    private RequestDto.ScriptBaseDto requestDto;
    private JenkinsInfo jenkinsInfo;
    private Script script;
    private UUID scriptId;

    @BeforeEach
    void setUp() {
        scriptId = UUID.randomUUID();
        requestDto = new RequestDto.ScriptBaseDto();
        requestDto.setScriptId(scriptId);
        requestDto.setInfoId(UUID.randomUUID());

        RequestDto.NotificationDto notification = RequestDto.NotificationDto.builder()
                .channel(com.example.backend.jenkins.notification.model.JobNotification.Channel.SLACK)
                .eventType(com.example.backend.jenkins.notification.model.JobNotification.EventType.BUILD_FAIL)
                .webhookUrl("webhook")
                .shouldNotify(true)
                .credentialName("credId")
                .build();
        requestDto.setNotificationList(List.of(notification));

        jenkinsInfo = JenkinsInfo.builder()
                .id(requestDto.getInfoId())
                .uri("http://jenkins")
                .build();

        script = Script.builder()
                .id(scriptId)
                .script("script")
                .build();
    }

    @Test
    @DisplayName("generateScript: scriptId가 존재하지만 DB에 없으면 예외 발생")
    void generateScript_scriptIdNotFound() {
        when(scriptRepository.existsById(scriptId)).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () ->
                scriptService.generateScript(requestDto));

        assertEquals(ErrorCode.JENKINS_SCRIPT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("generateScript: 기존 scriptId로 정상 생성 및 알림 동기화")
    void generateScript_existingScriptId_success() {
        Map<String, Object> context = Map.of("key", "value");

        when(scriptRepository.existsById(scriptId)).thenReturn(true);
        when(configService.buildScriptContext(requestDto)).thenReturn(context);
        when(configService.createScript(context)).thenReturn("rawScript");
        when(scriptEditUtil.injectBooleanParams("rawScript")).thenReturn("editedScript");
        when(jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId())).thenReturn(jenkinsInfo);
        when(jobNotificationService.getEnabledNotifications(any())).thenReturn(Collections.emptyList());
        when(jobNotificationService.updateScriptWithJobNotifications(any(), any()))
                .thenReturn(script);

        ResponseDto.LightScriptDto result = scriptService.generateScript(requestDto);

        assertNotNull(result);
        verify(jobNotificationService).syncJobNotifications(any(), eq(jenkinsInfo), any());
    }

    @Test
    @DisplayName("generateScript: 새 script 생성 및 알림 등록")
    void generateScript_newScript_success() {
        requestDto.setScriptId(null); // 새 script 생성 시나리오
        Map<String, Object> context = Map.of("key", "value");

        when(configService.buildScriptContext(requestDto)).thenReturn(context);
        when(configService.createScript(context)).thenReturn("rawScript");
        when(scriptEditUtil.injectBooleanParams("rawScript")).thenReturn("editedScript");
        when(jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId())).thenReturn(jenkinsInfo);
        when(scriptRepository.save(any())).thenReturn(script);
        when(jobNotificationService.getEnabledNotifications(any())).thenReturn(Collections.emptyList());
        when(jobNotificationService.updateScriptWithJobNotifications(any(), any()))
                .thenReturn(script);

        ResponseDto.LightScriptDto result = scriptService.generateScript(requestDto);

        assertNotNull(result);
        verify(scriptRepository).save(any());
        verify(jobNotificationService).createJobNotifications(any(), eq(jenkinsInfo), eq(scriptId));
    }

    @Test
    @DisplayName("generateScript: 알림 리스트가 null인 경우 예외 없이 동작")
    void generateScript_nullNotificationList() {
        requestDto.setNotificationList(null);

        Map<String, Object> context = Map.of("key", "value");

        when(scriptRepository.existsById(scriptId)).thenReturn(true);
        when(configService.buildScriptContext(requestDto)).thenReturn(context);
        when(configService.createScript(context)).thenReturn("rawScript");
        when(scriptEditUtil.injectBooleanParams("rawScript")).thenReturn("editedScript");
        when(jobNotificationService.getEnabledNotifications(any())).thenReturn(Collections.emptyList());
        when(jobNotificationService.updateScriptWithJobNotifications(any(), any()))
                .thenReturn(script);

        assertDoesNotThrow(() -> scriptService.generateScript(requestDto));
    }
}
