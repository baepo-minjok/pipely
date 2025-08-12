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

    private static final String ID_PARAM_LINE =
            "    string(name: 'ID', defaultValue: '', description: 'Job 식별자 (서버에 전달할 ID)')";

    // (1) 고정 콜백 스니펫
    private static final String STATUS_URL = "https://www.pipely.kro.kr/api/jenkins/build/status";
    private static final String FIXED_SUCCESS_SNIPPET =
            "sh \"\"\"\n" +
                    "curl -X POST \\\n" +
                    "     -H \"Content-Type: application/json\" \\\n" +
                    "     -d '{\n" +
                    "            \"success\": true,\n" +
                    "            \"jobId\": \"${params.ID}\"\n" +
                    "          }' \\\n" +
                    "     " + STATUS_URL + "\n" +
                    "\"\"\"";
    private static final String FIXED_FAILURE_SNIPPET =
            "sh \"\"\"\n" +
                    "curl -X POST \\\n" +
                    "     -H \"Content-Type: application/json\" \\\n" +
                    "     -d '{\n" +
                    "            \"success\": false,\n" +
                    "            \"jobId\": \"${params.ID}\"\n" +
                    "          }' \\\n" +
                    "     " + STATUS_URL + "\n" +
                    "\"\"\"";
    private final HttpClientService httpClientService;

    // 블록 범위 찾기 (startIdx는 여는 '{' 위치), 같은 레벨의 짝 '}' 위치를 반환
    private int findMatchingBrace(String src, int startIdx) {
        int depth = 0;
        for (int i = startIdx; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1; // 못 찾음
    }

    private String addToExistingParams(String block, List<String> paramLines) {
        // parameters { (body) }
        Pattern p = Pattern.compile("(?s)(parameters\\s*\\{)(.*?)(\\})");
        Matcher m = p.matcher(block);
        if (!m.find()) return block;

        String head = m.group(1), body = m.group(2), tail = m.group(3);

        // 이미 선언된 booleanParam 이름 수집
        Set<String> existing = new HashSet<>();
        Pattern namePat = Pattern.compile("booleanParam\\s*\\(\\s*name\\s*:\\s*'([A-Z0-9_]+)'");
        Matcher nm = namePat.matcher(body);
        while (nm.find()) existing.add(nm.group(1));

        // ID(string) 파라미터 존재 여부
        boolean hasId = Pattern.compile("(?m)\\bstring\\s*\\(\\s*name\\s*:\\s*['\\\"]ID['\\\"]")
                .matcher(body).find();

        // 추가할 booleanParam만 필터
        List<String> toAdd = paramLines.stream()
                .filter(line -> {
                    Matcher mm = Pattern.compile("name\\s*:\\s*'([A-Z0-9_]+)'").matcher(line);
                    return mm.find() && !existing.contains(mm.group(1));
                })
                .collect(Collectors.toList());

        String trimmedBody = body.strip();

        // ID가 없으면 최상단에 추가
        if (!hasId) {
            trimmedBody = (trimmedBody.isEmpty())
                    ? (ID_PARAM_LINE + "\n")
                    : (ID_PARAM_LINE + "\n" + trimmedBody + "\n");
        } else if (!trimmedBody.endsWith("\n") && !trimmedBody.isEmpty()) {
            trimmedBody = trimmedBody + "\n";
        }

        // booleanParam들 이어 붙이기
        if (!toAdd.isEmpty()) {
            trimmedBody += String.join("\n", toAdd) + "\n";
        }

        return head + "\n" + trimmedBody + tail;
    }

    private String buildParamsBlock(List<String> paramLines) {
        String joined = paramLines.stream().collect(Collectors.joining("\n"));
        if (!joined.isEmpty()) joined = "\n" + joined;
        return "parameters {\n" + ID_PARAM_LINE + joined + "\n}";
    }

    // 최상위 pipeline { ... } 범위 찾기
    private int[] findTopLevelPipeline(String src) {
        Matcher m = Pattern.compile("\\bpipeline\\s*\\{").matcher(src);
        if (!m.find()) return null;
        int open = src.indexOf("{", m.end() - 1);
        int close = findMatchingBrace(src, open);
        if (open < 0 || close < 0) return null;
        return new int[]{open, close};
    }

    // 최상위 특정 블록(예: parameters, post) 범위 찾기
    private int[] findTopLevelBlock(String src, String blockName) {
        int[] pipe = findTopLevelPipeline(src);
        if (pipe == null) return null;
        int openP = pipe[0], closeP = pipe[1];
        String body = src.substring(openP + 1, closeP);
        int bodyOffset = openP + 1;

        Matcher m = Pattern.compile("(?m)^\\s*" + blockName + "\\s*\\{").matcher(body);
        while (m.find()) {
            int open = body.indexOf("{", m.end() + m.start() - m.end()); // '{' at that match line
            open = body.indexOf("{", m.start()); // 보정
            int absOpen = bodyOffset + open;
            int absClose = findMatchingBrace(src, absOpen);
            // 최상위인지 확인: 해당 블록의 시작~끝이 pipeline 바디 범위 안이고, 추가 상위 블록 없이 바로 존재
            if (absClose > 0 && absClose <= closeP) {
                return new int[]{absOpen, absClose};
            }
        }
        return null;
    }

    // 들여쓰기 유틸
    private String indentN(String s, int n) {
        String pad = " ".repeat(n);
        return Arrays.stream(s.split("\\R", -1))
                .map(line -> line.isEmpty() ? "" : pad + line)
                .collect(Collectors.joining("\n"));
    }

    // 스테이지용 파라미터 키 생성 (RUN_BUILD → 대문자/비문자→_ 치환)
    private String stageFlag(String stageName) {
        return "RUN_" + stageName.toUpperCase().replaceAll("\\W+", "_");
    }

    public String injectToSuccessFailureBlocks(String script, String successScript, String failureScript) {
        final String defaultPipeline =
                "pipeline {\n" +
                        "  agent any\n" +
                        "  parameters {\n" +
                        "    string(name: 'ID', defaultValue: '', description: 'Job 식별자 (서버에 전달할 ID)')\n" +
                        "    booleanParam(name: 'RUN_EXAMPLE', defaultValue: true, description: 'Example 스테이지 실행 여부')\n" +
                        "  }\n" +
                        "  stages {\n" +
                        "    stage('Example') {\n" +
                        "      when { expression { return params.RUN_EXAMPLE ?: true } }\n" +
                        "      steps { echo 'noop' }\n" +
                        "    }\n" +
                        "  }\n" +
                        "  post {\n" +
                        "    success { echo 'SUCCESS' }\n" +
                        "    failure { echo 'FAILURE' }\n" +
                        "  }\n" +
                        "}\n";

        String result = (script == null || script.isBlank()) ? defaultPipeline : script;

        // declarative가 아니면 기본 템플릿으로 감쌀지, 아니면 그냥 반환할지 선택
        if (findTopLevelPipeline(result) == null) {
            // 여기선 보수적으로: 기본 템플릿 사용
            result = defaultPipeline;
        }

        // 최상위 post 블록 보장
        int[] post = findTopLevelBlock(result, "post");
        if (post == null) {
            int[] pipe = findTopLevelPipeline(result);
            int insertPos = pipe != null ? pipe[1] : result.length();
            String postBlock = "\n  post {\n    success { }\n    failure { }\n  }\n";
            result = result.substring(0, insertPos) + postBlock + result.substring(insertPos);
            post = findTopLevelBlock(result, "post");
        }

        // success/failure 블록 보장
        String postBody = result.substring(post[0] + 1, post[1]);
        boolean hasSuccess = Pattern.compile("\\bsuccess\\s*\\{").matcher(postBody).find();
        boolean hasFailure = Pattern.compile("\\bfailure\\s*\\{").matcher(postBody).find();

        if (!hasSuccess || !hasFailure) {
            String before = result.substring(0, post[0]);
            String block = result.substring(post[0], post[1] + 1);
            String after = result.substring(post[1] + 1);

            String inner = block.substring(1, block.length() - 1); // { ... }
            StringBuilder sb = new StringBuilder(inner);
            if (!hasSuccess) sb.append("\n    success { }\n");
            if (!hasFailure) sb.append("\n    failure { }\n");

            result = before + "{" + sb + "}" + after;
            post = findTopLevelBlock(result, "post");
        }

        // 내용 삽입: success/failure 여는 '{' 직후에 넣기
        if (successScript != null && !successScript.isBlank()) {
            result = insertIntoPostBlock(result, "success", successScript);
        }
        if (failureScript != null && !failureScript.isBlank()) {
            result = insertIntoPostBlock(result, "failure", failureScript);
        }
        return result;
    }

    private String insertIntoPostBlock(String src, String which, String snippet) {
        int[] post = findTopLevelBlock(src, "post");
        if (post == null) return src;
        // post 범위 안에서 해당 블록 찾기
        String body = src.substring(post[0] + 1, post[1]);
        int bodyOffset = post[0] + 1;

        Matcher m = Pattern.compile("\\b" + which + "\\s*\\{").matcher(body);
        if (!m.find()) return src;

        int open = body.indexOf("{", m.start());
        int absOpen = bodyOffset + open;
        // 여는 '{' 바로 뒤에 삽입
        String ins = "\n" + indentN(snippet.trim(), 6) + "\n";
        return src.substring(0, absOpen + 1) + ins + src.substring(absOpen + 1);
    }

    /**
     * 주어진 Jenkins pipeline script에서 모든 stage 이름을 추출합니다.
     */
    public List<String> extractStageNames(String script) {
        int[] pipe = findTopLevelPipeline(script);
        if (pipe == null) return Collections.emptyList();

        // 최상위 stages { ... } 범위 찾기
        int[] stages = findTopLevelBlock(script, "stages");
        if (stages == null) return Collections.emptyList();

        String stagesBody = script.substring(stages[0] + 1, stages[1]); // '{' 바로 뒤부터
        List<String> names = new ArrayList<>();

        // stages 블록 내부에서 최상위 stage(...) { ... }만 수집
        Pattern p = Pattern.compile("stage\\s*\\(\\s*['\\\"]([^'\\\"]+)['\\\"]\\s*\\)\\s*\\{");
        Matcher m = p.matcher(stagesBody);
        while (m.find()) {
            int open = stagesBody.indexOf("{", m.end() - m.start() + m.start()); // 보정
            open = stagesBody.indexOf("{", m.start());
            int absOpen = (stages[0] + 1) + open;
            int absClose = findMatchingBrace(script, absOpen);

            // parallel 블록 안에 있는 stage는 제외하고 싶다면 여기서 한 번 더 검사 가능
            names.add(m.group(1));
        }
        // 중복 제거, 순서 보존
        return names.stream().distinct().collect(Collectors.toList());
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
            String updated = addToExistingParams(block, paramLines); // ← 아래에서 ID 보장까지 처리
            return script.replace(block, updated);
        } else {
            // parameters 블록이 없으면 새로 만들 때 ID를 **항상** 첫 줄로 넣는다
            String newBlock = buildParamsBlock(paramLines);
            // pipeline { 다음 줄에 삽입
            return script.replaceFirst("(?m)(pipeline\\s*\\{)", "$1\n" + newBlock + "\n");
        }
    }


    private String insertWhenConditions(String script, List<String> stageNames) {
        if (stageNames.isEmpty()) return script;

        String result = script;
        for (String name : stageNames) {
            String flag = stageFlag(name);

            // 해당 stage 블록 경계 찾기
            Pattern hdr = Pattern.compile("stage\\s*\\(\\s*['\\\"]" + Pattern.quote(name) + "['\\\"]\\s*\\)\\s*\\{");
            Matcher mh = hdr.matcher(result);
            if (!mh.find()) continue;

            int open = result.indexOf("{", mh.end() - (mh.end() - mh.start()));
            open = result.indexOf("{", mh.start());
            int close = findMatchingBrace(result, open);
            if (open < 0 || close < 0) continue;

            String stageBody = result.substring(open + 1, close);

            // 이미 when 있으면 스킵
            if (Pattern.compile("\\bwhen\\s*\\{").matcher(stageBody).find()) continue;

            // steps 위치 찾기 (있으면 그 앞에 when 넣음, 없으면 stage 헤더 직후에)
            Matcher ms = Pattern.compile("\\bsteps\\s*\\{").matcher(stageBody);
            int injectAt = (ms.find()) ? (open + 1 + ms.start()) : (open + 1);

            String whenBlock = "\n      when {\n        expression { params." + flag + " ?: true }\n      }\n";
            result = result.substring(0, injectAt) + whenBlock + result.substring(injectAt);
        }
        return result;
    }


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

    private String insertIntoTopPostSection(String src, String which, String snippet) {
        int[] post = findTopLevelBlock(src, "post");
        if (post == null) return src;

        String body = src.substring(post[0] + 1, post[1]);
        int bodyOffset = post[0] + 1;

        Matcher m = Pattern.compile("\\b" + which + "\\s*\\{").matcher(body);
        if (!m.find()) return src;

        int absOpen = bodyOffset + body.indexOf("{", m.start());
        String ins = "\n" + indentN(snippet.trim(), 6) + "\n";
        return src.substring(0, absOpen + 1) + ins + src.substring(absOpen + 1);
    }

    private boolean topPostSectionContains(String src, String which, String needle) {
        int[] post = findTopLevelBlock(src, "post");
        if (post == null) return false;
        String body = src.substring(post[0] + 1, post[1]);
        Matcher m = Pattern.compile("(?s)\\b" + which + "\\s*\\{.*?" + Pattern.quote(needle) + ".*?\\}").matcher(body);
        return m.find();
    }

    public String ensureStatusCallbackPost(String script) {
        final String defaultPipeline =
                "pipeline {\n" +
                        "  agent any\n" +
                        "  parameters {\n" +
                        "    string(name: 'ID', defaultValue: '', description: 'Job 식별자 (서버에 전달할 ID)')\n" +
                        "    booleanParam(name: 'RUN_EXAMPLE', defaultValue: true, description: 'Example 스테이지 실행 여부')\n" +
                        "  }\n" +
                        "  stages {\n" +
                        "    stage('Example') {\n" +
                        "      when { expression { return params.RUN_EXAMPLE ?: true } }\n" +
                        "      steps { echo 'noop' }\n" +
                        "    }\n" +
                        "  }\n" +
                        "  post {\n" +
                        "    success { echo 'SUCCESS' }\n" +
                        "    failure { echo 'FAILURE' }\n" +
                        "  }\n" +
                        "}\n";

        String result = (script == null || script.isBlank()) ? defaultPipeline : script;

        if (findTopLevelPipeline(result) == null) result = defaultPipeline;

        int[] post = findTopLevelBlock(result, "post");
        if (post == null) {
            int[] pipe = findTopLevelPipeline(result);
            int insertPos = (pipe != null) ? pipe[1] : result.length();
            String postBlock = "\n  post {\n    success { }\n    failure { }\n  }\n";
            result = result.substring(0, insertPos) + postBlock + result.substring(insertPos);
            post = findTopLevelBlock(result, "post");
        }

        String before = result.substring(0, post[0]);
        String block = result.substring(post[0], post[1] + 1);
        String after = result.substring(post[1] + 1);

        boolean hasSuccess = Pattern.compile("\\bsuccess\\s*\\{").matcher(block).find();
        boolean hasFailure = Pattern.compile("\\bfailure\\s*\\{").matcher(block).find();

        if (!hasSuccess || !hasFailure) {
            String inner = block.substring(1, block.length() - 1); // { ... }
            StringBuilder sb = new StringBuilder(inner.trim());
            if (!hasSuccess) sb.append("\n    success { }\n");
            if (!hasFailure) sb.append("\n    failure { }\n");
            result = before + "{" + "\n" + sb.toString().trim() + "\n" + "}" + after;
            post = findTopLevelBlock(result, "post");
        }

        if (!topPostSectionContains(result, "success", STATUS_URL)) {
            result = insertIntoTopPostSection(result, "success", FIXED_SUCCESS_SNIPPET);
        }
        if (!topPostSectionContains(result, "failure", STATUS_URL)) {
            result = insertIntoTopPostSection(result, "failure", FIXED_FAILURE_SNIPPET);
        }

        return result;
    }

    private String indent8(String s) {
        return Arrays.stream(s.split("\\R", -1))
                .map(line -> "        " + line) // 8칸 들여쓰기
                .collect(Collectors.joining("\n"));
    }

    // (3) 간단 포맷터: 들여쓰기/줄바꿈 정리 (트리플쿼트 내부는 건드리지 않음)
    public String prettyFormat(String s) {
        if (s == null) return "";
        String src = s.replace("\r\n", "\n");

        StringBuilder out = new StringBuilder();
        int depth = 0;
        boolean inTripleDouble = false;
        boolean inTripleSingle = false;

        String[] lines = src.split("\n", -1);
        for (String rawLine : lines) {
            String line = rawLine;

            // 트리플 따옴표 토글 감지 (라인 내 개수로 토글)
            if (!inTripleSingle) {
                int c = countOccurrences(line, "\"\"\"");
                if (c % 2 != 0) inTripleDouble = !inTripleDouble;
            }
            if (!inTripleDouble) {
                int c = countOccurrences(line, "'''");
                if (c % 2 != 0) inTripleSingle = !inTripleSingle;
            }

            if (inTripleDouble || inTripleSingle) {
                // 트리플 문자열 내부는 원형 유지
                out.append(line).append("\n");
                continue;
            }

            String trimmed = line.trim();

            // 라인이 '}'로 시작하면 먼저 depth 감소
            if (trimmed.startsWith("}")) depth = Math.max(0, depth - 1);

            // 들여쓰기(2칸)
            String indent = "  ".repeat(depth);
            out.append(trimmed.isEmpty() ? "" : indent + trimmed).append("\n");

            // 현재 라인의 brace로 다음 depth 계산
            int opens = countOccurrences(trimmed, "{");
            int closes = countOccurrences(trimmed, "}");
            depth += (opens - closes);
            if (depth < 0) depth = 0;
        }

        // post 내 success/failure 같은 조건 블록 사이에 공백 라인 하나 유지(가독성)
        String pretty = out.toString()
                .replaceAll("(?m)^(\\s*success\\s*\\{)\\n\\s*\\n", "$1\n")
                .replaceAll("(?m)^(\\s*failure\\s*\\{)\\n\\s*\\n", "$1\n");

        return pretty.trim() + "\n";
    }

    private int countOccurrences(String str, String token) {
        if (str == null || token == null || token.isEmpty()) return 0;
        int count = 0, idx = 0;
        while ((idx = str.indexOf(token, idx)) != -1) {
            count++;
            idx += token.length();
        }
        return count;
    }

}