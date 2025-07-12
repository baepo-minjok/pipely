package com.example.backend.util;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScriptEditUtil {

    private static final Pattern STAGE_PATTERN = Pattern.compile(
            "stage\\s*\\(\\s*['\\\"]([^'\\\"]+)['\\\"]\\s*\\)\\s*\\{"
    );
    private final HttpClientService httpClientService;

    /**
     * 주어진 Jenkins pipeline script에서 모든 stage 이름을 추출합니다.
     */
    public List<String> extractStageNames(String script) {
        Matcher matcher = STAGE_PATTERN.matcher(script);
        LinkedHashSet<String> stages = new LinkedHashSet<>();
        while (matcher.find()) {
            stages.add(matcher.group(1));
        }
        return new ArrayList<>(stages);
    }

    /**
     * 스크립트에 파라미터 블록과 when 제어 로직을 삽입합니다.
     */
    public String injectBooleanParams(String script) {
        List<String> stageNames = extractStageNames(script);

        // 1) 파라미터 정의 리스트 생성
        List<String> paramLines = stageNames.stream()
                .map(name -> {
                    String safe = "RUN_" + name.toUpperCase().replaceAll("\\W+", "_");
                    return String.format(
                            "    booleanParam(name: '%s', defaultValue: true, description: '%s 스테이지 실행 여부')",
                            safe, name
                    );
                })
                .collect(Collectors.toList());

        // 2) properties 블록 추가 또는 수정
        script = mergeOrInsertParameters(script, paramLines);

        // 3) 각 stage에 when 삽입
        script = insertWhenConditions(script, stageNames);

        return script;
    }

    private String mergeOrInsertParameters(String script, List<String> paramLines) {
        Pattern paramsBlock = Pattern.compile("(?s)parameters\\s*\\{.*?\\}", Pattern.CASE_INSENSITIVE);
        Matcher m = paramsBlock.matcher(script);
        if (m.find()) {
            String block = m.group();
            String updated = addToExistingParams(block, paramLines);
            return script.replace(block, updated);
        } else {
            String newBlock = buildParamsBlock(paramLines);
            // pipeline { 다음 줄에 바로 삽입
            return script.replaceFirst("(?m)(pipeline\\s*\\{)", "$1\n" + newBlock + "\n");
        }
    }

    private String addToExistingParams(String block, List<String> paramLines) {
        // parameters { (body) }
        Pattern p = Pattern.compile("(?s)(parameters\\s*\\{)(.*?)(\\})");
        Matcher m = p.matcher(block);
        if (!m.find()) return block;
        String head = m.group(1), body = m.group(2), tail = m.group(3);

        // 이미 선언된 이름 수집
        Set<String> existing = new HashSet<>();
        Pattern namePat = Pattern.compile("booleanParam\\s*\\(\\s*name\\s*:\\s*'([A-Z0-9_]+)'");
        Matcher nm = namePat.matcher(body);
        while (nm.find()) existing.add(nm.group(1));

        // 추가할 라인만 필터
        List<String> toAdd = paramLines.stream()
                .filter(line -> {
                    Matcher mm = namePat.matcher(line);
                    return mm.find() && !existing.contains(mm.group(1));
                })
                .collect(Collectors.toList());

        String newBody = body.trim();
        if (!toAdd.isEmpty()) {
            newBody += "\n" + String.join("\n", toAdd) + "\n";
        }

        return head + "\n" + newBody + tail;
    }

    private String buildParamsBlock(List<String> paramLines) {
        String joined = paramLines.stream().collect(Collectors.joining("\n"));
        return "parameters {\n" + joined + "\n}";
    }

    private String insertWhenConditions(String script, List<String> stageNames) {
        String result = script;
        for (String name : stageNames) {
            String safe = "RUN_" + name.toUpperCase().replaceAll("\\W+", "_");
            String regex = "(?m)(stage\\s*\\(\\s*['\"]" + Pattern.quote(name) + "['\"]\\s*\\)\\s*\\{)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                int start = matcher.end(1);
                int endIdx = result.indexOf('}', start);
                String between = endIdx > start ? result.substring(start, endIdx) : "";
                if (Pattern.compile("\\bwhen\\b").matcher(between).find()) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1)));
                } else {
                    String replacement = matcher.group(1)
                            + "\n      when { expression { params." + safe + " } }";
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }


    /**
     * Jenkins Declarative Pipeline 스크립트 문법 검증.
     *
     * @param info   Jenkins 접속 정보 (URI, 계정, 토큰)
     * @param script Script 전체 텍스트
     * @return 문법이 유효하면 true, 오류가 있으면 false
     */
    public boolean validateJenkinsfile(JenkinsInfo info, String script) {
        String url = info.getUri().replaceAll("/+$", "") + "/pipeline-model-converter/validate";

        HttpHeaders headers = httpClientService.buildHeaders(
                info,
                MediaType.APPLICATION_FORM_URLENCODED
        );

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("jenkinsfile", script);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        String response = httpClientService.exchange(url, HttpMethod.POST, request, String.class);

        return response.contains("Jenkinsfile successfully validated.");
    }
}