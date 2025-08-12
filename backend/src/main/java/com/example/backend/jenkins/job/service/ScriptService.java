package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ScriptMode;
import com.example.backend.jenkins.job.repository.ScriptRepository;
import com.example.backend.util.ScriptEditUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScriptService {

    private final ConfigService configService;
    private final ScriptEditUtil scriptEditUtil;
    private final ScriptRepository scriptRepository;
    private final JenkinsInfoService jenkinsInfoService;

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
    public Script generateScript(RequestDto.ScriptBaseDto requestDto) {
        Script script = (requestDto.getScriptId() != null)
                ? updateExistingScript(requestDto)
                : createNewScript(requestDto);

        return scriptRepository.save(script);
    }

    @Transactional
    public void deleteScript(UUID scriptId) {
        scriptRepository.deleteById(scriptId);
    }

    public void validateScript(RequestDto.ScriptValidateDto requestDto) {
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());
        String candidate = requestDto.getManualScript() != null
                ? requestDto.getManualScript()
                : requestDto.getScript();

        if (!scriptEditUtil.validateJenkinsfile(info, candidate)) {
            throw new CustomException(ErrorCode.JENKINS_SCRIPT_INVALID);
        }
    }


    private Script createNewScript(RequestDto.ScriptBaseDto dto) {
        ScriptMode mode = dto.getMode() == null ? ScriptMode.GENERATED : dto.getMode();
        String content = switch (mode) {
            case MANUAL -> dto.getManualScript();
            case GENERATED -> configService.createScript(configService.buildScriptContext(dto));
        };

        content = scriptEditUtil.injectBooleanParams(content);
        return Script.toEntity(dto, content);
    }

    private Script updateExistingScript(RequestDto.ScriptBaseDto dto) {
        Script existing = getScriptById(dto.getScriptId());
        if (!scriptRepository.existsById(dto.getScriptId())) {
            throw new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND);
        }

        ScriptMode mode = dto.getMode() == null ? existing.getMode() : dto.getMode();
        String content = switch (mode) {
            case MANUAL -> dto.getManualScript();
            case GENERATED -> configService.createScript(configService.buildScriptContext(dto));
        };

        content = scriptEditUtil.injectBooleanParams(content);

        applyDtoToScript(existing, dto, content);

        return existing;
    }

    private void applyDtoToScript(Script script, RequestDto.ScriptBaseDto dto, String injectedScript) {
        // 기본 필드
        script.setMode(dto.getMode());
        script.setGithubUrl(dto.getGithubUrl());
        script.setBranch(dto.getBranch());
        script.setIsBuildSelected(dto.getIsBuildSelected());
        script.setIsTestSelected(dto.getIsTestSelected());
        script.setIsK8sDeploy(dto.getIsK8sDeploy());
        script.setTag(dto.getTag());
        script.setK8sPath(dto.getK8sPath());
        script.setDeploymentName(dto.getDeploymentName());
        script.setNamespace(dto.getNamespace());
        script.setAppName(dto.getAppName());
        script.setContainerName(dto.getContainerName());
        script.setImageRepo(dto.getImageRepo());
        script.setPort(dto.getPort());
        script.setReplicas(dto.getReplicas());
        script.setIsEc2Deploy(dto.getIsEc2Deploy());
        script.setEc2DeployPath(dto.getEc2DeployPath());
        script.setSshKeyPath(dto.getSshKeyPath());
        script.setSshPort(dto.getSshPort());
        script.setDeployTarget(dto.getDeployTarget());

        script.setScript(injectedScript);
    }

}
