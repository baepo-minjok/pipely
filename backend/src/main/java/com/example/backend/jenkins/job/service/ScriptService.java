package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScriptService {

    private final ConfigService configService;
    private final ScriptRepository scriptRepository;

    public Script getScriptById(UUID scriptId) {
        Optional<Script> optionalScript = scriptRepository.findById(scriptId);
        Script script = null;
        if (optionalScript.isPresent()) {
            script = optionalScript.get();
        }
        return script;
    }
    
    public ResponseDto.LightScriptDto generateScript(RequestDto.ScriptBaseDto requestDto) {
        UUID scriptId = requestDto.getScriptId();
        Script newScript = null;
        if (scriptId != null) {
            if (!scriptRepository.existsById(scriptId)) {
                throw new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND);
            }
            String script = configService.createScript(configService.buildScriptContext(requestDto));
            newScript = Script.toEntity(requestDto, script);
            newScript.setId(scriptId);

            newScript = scriptRepository.save(newScript);
        } else {
            String script = configService.createScript(configService.buildScriptContext(requestDto));

            newScript = scriptRepository.save(Script.toEntity(requestDto, script));
        }

        return ResponseDto.entityToLightScriptDto(newScript);
    }
}
