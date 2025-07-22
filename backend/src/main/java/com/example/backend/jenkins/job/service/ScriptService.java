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
    public ResponseDto.LightScriptDto generateScript(RequestDto.ScriptBaseDto requestDto, String userName) {
        UUID scriptId = requestDto.getScriptId();
        Script newScript = null;
        String script = null;
        if (scriptId != null) {
            if (!scriptRepository.existsById(scriptId)) {
                throw new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND);
            }
            script = configService.createScript(configService.buildScriptContext(requestDto));

            String injectedScript = scriptEditUtil.injectBooleanParams(script);

            newScript = Script.toEntity(requestDto, injectedScript);
            newScript.setId(scriptId);

            //newScript = scriptRepository.save(newScript);

            if (requestDto.getNotificationList() != null) {
                JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());
                jobNotificationService.syncJobNotifications(requestDto.getNotificationList(), info, newScript);
            }
        } else {
            script = configService.createScript(configService.buildScriptContext(requestDto));

            String injectedScript = scriptEditUtil.injectBooleanParams(script);
            newScript = Script.toEntity(requestDto, injectedScript);
            //newScript = scriptRepository.save(Script.toEntity(requestDto, injectedScript));

            if (requestDto.getNotificationList() != null) {
                JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());
                jobNotificationService.createJobNotifications(requestDto.getNotificationList(), info, newScript.getId());
            }
        }

        List<JobNotification> enabledNotifications = jobNotificationService.getEnabledNotifications(newScript);

        newScript = jobNotificationService.updateScriptWithJobNotifications(newScript, enabledNotifications, userName);

        return ResponseDto.entityToLightScriptDto(newScript);
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

}
