package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
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
    public ResponseDto.LightScriptDto generateScript(RequestDto.ScriptBaseDto requestDto) {
        Script script;

        if (requestDto.getScriptId() != null) {
            script = updateExistingScript(requestDto);
        } else {
            script = createNewScript(requestDto);
        }

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
        Script existingScript = getScriptById(scriptId);
        if (!scriptRepository.existsById(scriptId)) {
            throw new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND);
        }

        String scriptContent = configService.createScript(configService.buildScriptContext(dto));
        String injectedScript = scriptEditUtil.injectBooleanParams(scriptContent);
        
        applyDtoToScript(existingScript, dto, injectedScript);
        return existingScript;
    }

    private void applyDtoToScript(Script script, RequestDto.ScriptBaseDto dto, String injectedScript) {
        // 기본 필드
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
