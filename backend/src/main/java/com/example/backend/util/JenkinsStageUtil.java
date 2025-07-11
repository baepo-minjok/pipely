package com.example.backend.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JenkinsStageUtil {

    private static final Pattern STAGE_PATTERN = Pattern.compile(
            "stage\\s*\\(\\s*['\\\"]([^'\\\"]+)['\\\"]\\s*\\)\\s*\\{"
    );

    /**
     * 주어진 Jenkins pipeline script에서 모든 stage 이름을 추출합니다.
     */
    public static List<String> extractStageNames(String script) {
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
    public static String injectBooleanParams(String script) {
        List<String> stageNames = extractStageNames(script);
        // 1) booleanParam 블록 생성
        String paramsBlock = stageNames.stream()
                .map(name -> {
                    // safe param name: 영문 대문자+_ 로
                    String safe = "RUN_" + name.toUpperCase().replaceAll("\\W+", "_");
                    return String.format(
                            "    booleanParam(name: '%s', defaultValue: true, description: '%s 스테이지 실행 여부')",
                            safe, name
                    );
                })
                .collect(Collectors.joining(",\n"));
        paramsBlock = "properties([\n  parameters([\n" + paramsBlock + "\n  ])\n])";

        // 2) pipeline { 아래에 삽입
        String withParams = script.replaceFirst(
                "(pipeline\\s*\\{)",
                "$1\n" + paramsBlock + "\n"
        );

        // 3) 각 stage 에 when 추가
        String controlled = withParams;
        for (String name : stageNames) {
            String safe = "RUN_" + name.toUpperCase().replaceAll("\\W+", "_");
            String match = "stage\\s*\\(\\s*['\\\"]" + Pattern.quote(name) + "['\\\"]\\s*\\)\\s*\\{";
            String replacement = String.join("\n",
                    "stage('" + name + "') {",
                    "  when { expression { params." + safe + " } }"
            );
            controlled = controlled.replaceAll(
                    match,
                    Matcher.quoteReplacement(replacement)
            );
        }

        return controlled;
    }

}