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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
     * 스크립트에 TARGET_STAGE 파라미터와 when 제어 로직을 삽입합니다.
     *
     * @param script 원본 Jenkinsfile 내용
     * @return 제어 로직이 추가된 새로운 스크립트
     */
    public String injectBooleanParams(String script) {
        List<String> stageNames = extractStageNames(script);

        // 1) booleanParam 블록 생성 (기존 코드와 동일)
        String paramsBlock = stageNames.stream()
                .map(name -> {
                    String safe = "RUN_" + name.toUpperCase().replaceAll("\\W+", "_");
                    return String.format(
                            "    booleanParam(name: '%s', defaultValue: true, description: '%s 스테이지 실행 여부')",
                            safe, name
                    );
                })
                .collect(Collectors.joining(",\n"));
        paramsBlock = "properties([\n  parameters([\n" + paramsBlock + "\n  ])\n])";

        // 2) pipeline { 아래에 삽입
        String controlled = script.replaceFirst(
                "(pipeline\\s*\\{)",
                "$1\n" + paramsBlock + "\n"
        );

        // 3) 각 stage 에 조건부 삽입
        for (String name : stageNames) {
            String safe = "RUN_" + name.toUpperCase().replaceAll("\\W+", "_");

            // single or double quote 모두 잡는 regex
            String matchRegex =
                    "(?m)(stage\\s*\\(\\s*['\\\"]"
                            + Pattern.quote(name)
                            + "['\\\"]\\s*\\)\\s*\\{)";

            // 이미 when 이 있는지 간단히 보기 위해,
            // 이 스테이지 블록 시작점부터 첫번째 「}」 앞까지 짜르고 contains 검사
            Pattern p = Pattern.compile(matchRegex);
            Matcher m = p.matcher(controlled);
            if (!m.find()) continue;  // 아예 스테이지가 없으면 skip

            int start = m.start(1);
            int endOfBlock = controlled.indexOf("\n}", start);
            String snippet = endOfBlock > 0
                    ? controlled.substring(start, endOfBlock)
                    : controlled.substring(start);

            // 이미 when 블록이 있다면 삽입하지 않음
            if (snippet.contains("when")) continue;

            // replacement: 그룹(1) 바로 뒤에 when 구문 추가
            String replacement = ""
                    + "      when { expression { params." + safe + " } }";

            controlled = controlled.replaceFirst(matchRegex,
                    Matcher.quoteReplacement(replacement));
        }

        return controlled;
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