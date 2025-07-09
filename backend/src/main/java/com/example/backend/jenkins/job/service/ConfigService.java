package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final MustacheFactory mf;

    // config.xml 파일 템플릿 생성하는 메서드
    public String createConfig(Map<String, Object> context) {
        Mustache mustache = null;

        try {
            mustache = mf.compile("template/config.mustache");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_FILE_NOT_FOUND);
        }

        StringWriter writer = new StringWriter();
        try {
            mustache.execute(writer, context).flush();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_EXECUTE_FAILED);
        }

        return writer.toString();
    }

    // script 템플릿 생성하는 메서드
    public String createScript(Map<String, Object> context) {

        Mustache mustache = null;

        try {
            mustache = mf.compile("template/script.mustache");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_FILE_NOT_FOUND);
        }

        StringWriter writer = new StringWriter();
        try {
            mustache.execute(writer, context).flush();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_EXECUTE_FAILED);
        }

        return writer.toString();
    }

    // script 템플릿에 사용되는 context Map 만드는 함수
    public Map<String, Object> buildScriptContext(RequestDto.CreateDto createDto) {

        Map<String, Object> context = new HashMap<>();

        context.put(createDto.getBuildTool(), createDto.getBuildTool());
        context.put("directory", createDto.getDirectory());
        context.put("isBuildSelected", createDto.getIsBuildSelected());
        context.put("branch", createDto.getBranch());
        context.put("githubUrl", createDto.getGithubUrl());

        return context;
    }

    // config 템플릿에 사용되는 context Map 만드는 함수
    public Map<String, Object> buildConfigContext(RequestDto.CreateDto createDto) {

        // script 생성
        Map<String, Object> scriptContext = buildScriptContext(createDto);
        String script = createScript(scriptContext);

        Map<String, Object> context = new HashMap<>();

        context.put("description", createDto.getDescription());
        context.put("githubUrl", createDto.getGithubUrl());
        context.put("script", script);
        context.put("trigger", createDto.getTrigger());

        return context;
    }
}
