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
import com.example.backend.jenkins.notification.repository.JobNotificationRepository;
import com.example.backend.jenkins.notification.service.JobNotificationService;
import com.example.backend.util.ScriptEditUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScriptService {

    private final ConfigService configService;
    private final ScriptEditUtil scriptEditUtil;
    private final ScriptRepository scriptRepository;
    private final JenkinsInfoService jenkinsInfoService;
    private final JobNotificationService jobNotificationService;
    private final JobNotificationRepository jobNotificationRepository;

    public Script getScriptById(UUID scriptId) {

        return scriptRepository.findById(scriptId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND));
    }

    /**
     * Script 만들어서 LightScriptDto로 반환
     *
     * @param requestDto SCriptBaseDto 타입
     * @return
     */
    public ResponseDto.LightScriptDto generateScript(RequestDto.ScriptBaseDto requestDto) {
        Script script;

        if (requestDto.getScriptId() != null) {
            script = updateExistingScript(requestDto);
        } else {
            script = createNewScript(requestDto);
            script = scriptRepository.save(script);
        }

        handleNotifications(script, requestDto);

        List<JobNotification> enabledNotifications = jobNotificationService.getEnabledNotifications(script);
        String updatedScript = scriptEditUtil.injectNotificationPostBlock(script.getScript(), enabledNotifications);
        script.setScript(updatedScript);

        script = scriptRepository.save(script);

        return ResponseDto.entityToLightScriptDto(script);
    }

    @Transactional
    public void deleteScript(UUID scriptId) {
        scriptRepository.deleteById(scriptId);
    }

    public void validateScript(RequestDto.ScriptValidateDto requestDto) {

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());

        if (!scriptEditUtil.validateJenkinsfile(info, requestDto.getScript())) {
            throw new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND);
        }

    }

    private Script createNewScript(RequestDto.ScriptBaseDto dto) {
        String scriptContent = configService.createScript(configService.buildScriptContext(dto));
        String injectedScript = scriptEditUtil.injectBooleanParams(scriptContent);
        return Script.toEntity(dto, injectedScript);
    }

    private Script updateExistingScript(RequestDto.ScriptBaseDto dto) {
        UUID scriptId = dto.getScriptId();
        if (!scriptRepository.existsById(scriptId)) {
            throw new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND);
        }

        String scriptContent = configService.createScript(configService.buildScriptContext(dto));
        String injectedScript = scriptEditUtil.injectBooleanParams(scriptContent);

        Script script = Script.toEntity(dto, injectedScript);
        script.setId(scriptId);
        return script;
    }

    private void handleNotifications(Script script, RequestDto.ScriptBaseDto dto) {
        List<RequestDto.NotificationDto> notiList = dto.getNotificationList();
        if (notiList == null || notiList.isEmpty()) return;

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());

        if (dto.getScriptId() != null) {
            jobNotificationService.syncJobNotifications(notiList, info, script);
        } else {
            jobNotificationService.createJobNotifications(notiList, info, script.getId());
        }
    }

}
