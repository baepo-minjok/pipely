package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.ScriptRepository;
import com.example.backend.jenkins.notification.model.JobNotification;
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

    private RequestDto.ScriptBaseDto baseDto;
    private JenkinsInfo jenkinsInfo;
    private Script script;
    private UUID scriptId;

    @BeforeEach
    void setUp() {
        scriptId = UUID.randomUUID();
        baseDto = new RequestDto.ScriptBaseDto();
        baseDto.setScriptId(scriptId);
        baseDto.setInfoId(UUID.randomUUID());

        RequestDto.NotificationDto notification = RequestDto.NotificationDto.builder()
                .channel(JobNotification.Channel.SLACK)
                .eventType(JobNotification.EventType.BUILD_FAIL)
                .webhookUrl("webhook")
                .shouldNotify(true)
                .credentialName("credId")
                .build();
        baseDto.setNotificationList(List.of(notification));

        jenkinsInfo = JenkinsInfo.builder()
                .id(baseDto.getInfoId())
                .uri("http://jenkins")
                .build();

        script = Script.builder()
                .id(scriptId)
                .script("script")
                .build();
    }

    @Test
    @DisplayName("generateScript: scriptId 존재하지만 DB에 없으면 예외 발생")
    void generateScript_scriptIdNotFound() {
        when(scriptRepository.existsById(scriptId)).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () ->
                scriptService.generateScript(baseDto));

        assertEquals(ErrorCode.JENKINS_SCRIPT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("generateScript: 기존 scriptId로 정상 생성 및 알림 동기화 및 스크립트 수정")
    void generateScript_existingScriptId_success() {
        Map<String, Object> context = Map.of("key", "value");
        List<JobNotification> notis = List.of(
                JobNotification.builder().eventType(JobNotification.EventType.BUILD_SUCCESS).build()
        );

        when(scriptRepository.existsById(scriptId)).thenReturn(true);
        when(configService.buildScriptContext(baseDto)).thenReturn(context);
        when(configService.createScript(context)).thenReturn("rawScript");
        when(scriptEditUtil.injectBooleanParams("rawScript")).thenReturn("booleanInjectedScript");
        when(jenkinsInfoService.getJenkinsInfo(baseDto.getInfoId())).thenReturn(jenkinsInfo);
        when(jobNotificationService.getEnabledNotifications(any())).thenReturn(notis);
        when(scriptEditUtil.injectNotificationPostBlock("booleanInjectedScript", notis)).thenReturn("finalScript");
        when(scriptRepository.save(any())).thenReturn(script);

        ResponseDto.LightScriptDto result = scriptService.generateScript(baseDto);

        assertNotNull(result);
        verify(jobNotificationService).syncJobNotifications(any(), eq(jenkinsInfo), any());
        verify(scriptRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("generateScript: 새 script 생성 및 알림 등록")
    void generateScript_newScript_success() {
        baseDto.setScriptId(null);
        Map<String, Object> context = Map.of("x", "y");
        List<JobNotification> notis = Collections.emptyList();

        UUID newScriptId = UUID.randomUUID();
        Script newScript = Script.toEntity(baseDto, "booleanInjectedScript");
        newScript.setId(newScriptId);

        when(configService.buildScriptContext(baseDto)).thenReturn(context);
        when(configService.createScript(context)).thenReturn("rawScript");
        when(scriptEditUtil.injectBooleanParams("rawScript")).thenReturn("booleanInjectedScript");
        when(scriptRepository.save(any()))
                .thenReturn(newScript)
                .thenReturn(newScript); // save 2번
        when(jenkinsInfoService.getJenkinsInfo(baseDto.getInfoId())).thenReturn(jenkinsInfo);
        when(jobNotificationService.getEnabledNotifications(any())).thenReturn(notis);
        when(scriptEditUtil.injectNotificationPostBlock("booleanInjectedScript", notis)).thenReturn("finalScript");

        ResponseDto.LightScriptDto result = scriptService.generateScript(baseDto);

        assertNotNull(result);
        verify(jobNotificationService).createJobNotifications(any(), eq(jenkinsInfo), eq(newScriptId));
        verify(scriptRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("generateScript: 알림 리스트가 null이면 알림 생성 없이 동작")
    void generateScript_nullNotificationList() {
        baseDto.setNotificationList(null);
        when(scriptRepository.existsById(scriptId)).thenReturn(true);
        when(configService.buildScriptContext(baseDto)).thenReturn(Map.of());
        when(configService.createScript(any())).thenReturn("rawScript");
        when(scriptEditUtil.injectBooleanParams("rawScript")).thenReturn("booleanInjectedScript");
        when(jobNotificationService.getEnabledNotifications(any())).thenReturn(Collections.emptyList());
        when(scriptEditUtil.injectNotificationPostBlock(any(), any())).thenReturn("finalScript");
        when(scriptRepository.save(any())).thenReturn(script);

        assertDoesNotThrow(() -> scriptService.generateScript(baseDto));

        verify(jobNotificationService, never()).createJobNotifications(any(), any(), any());
        verify(jobNotificationService, never()).syncJobNotifications(any(), any(), any());
    }

    @Test
    @DisplayName("validateScript: 유효한 스크립트일 경우 예외 발생하지 않음")
    void validateScript_valid() {
        RequestDto.ScriptValidateDto validateDto = new RequestDto.ScriptValidateDto();
        validateDto.setInfoId(jenkinsInfo.getId());
        validateDto.setScript("validScript");

        when(jenkinsInfoService.getJenkinsInfo(jenkinsInfo.getId())).thenReturn(jenkinsInfo);
        when(scriptEditUtil.validateJenkinsfile(jenkinsInfo, "validScript")).thenReturn(true);

        assertDoesNotThrow(() -> scriptService.validateScript(validateDto));
    }

    @Test
    @DisplayName("validateScript: 유효하지 않으면 예외 발생")
    void validateScript_invalid() {
        RequestDto.ScriptValidateDto validateDto = new RequestDto.ScriptValidateDto();
        validateDto.setInfoId(jenkinsInfo.getId());
        validateDto.setScript("invalidScript");

        when(jenkinsInfoService.getJenkinsInfo(jenkinsInfo.getId())).thenReturn(jenkinsInfo);
        when(scriptEditUtil.validateJenkinsfile(jenkinsInfo, "invalidScript")).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class,
                () -> scriptService.validateScript(validateDto));

        assertEquals(ErrorCode.JENKINS_SCRIPT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("deleteScript: script 삭제 호출")
    void deleteScript_success() {
        UUID id = UUID.randomUUID();
        assertDoesNotThrow(() -> scriptService.deleteScript(id));
        verify(scriptRepository).deleteById(id);
    }
}
