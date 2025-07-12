package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.util.CronExpressionUtil;
import com.example.backend.util.ScriptEditUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {

    private final RestTemplate restTemplate;
    private final ScriptEditUtil scriptEditUtil;
    private final MustacheFactory mf;

    public static String[] parseOwnerAndRepo(String gitUrl) {
        try {
            URI uri = new URI(gitUrl);
            String host = uri.getHost();
            if (!"github.com".equalsIgnoreCase(host)) {
                throw new IllegalArgumentException("GitHub URL이 아닙니다: " + gitUrl);
            }
            String path = uri.getPath();
            String[] segments = path.split("/");
            if (segments.length < 3) {
                throw new IllegalArgumentException("리포지토리 경로가 올바르지 않습니다: " + path);
            }
            String owner = segments[1];
            String repo = segments[2].endsWith(".git")
                    ? segments[2].substring(0, segments[2].length() - 4)
                    : segments[2];
            return new String[]{owner, repo};
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("잘못된 URL 형식: " + gitUrl, e);
        }
    }

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
    public Map<String, Object> buildScriptContext(RequestDto.ScriptBaseDto scriptDto) {

        Map<String, Object> context = new HashMap<>();

        Map<String, String> buildTool = findBuildTool(scriptDto.getGithubUrl());

        String branch = scriptDto.getBranch() == null ? "main" : scriptDto.getBranch();
        String githubUrl = scriptDto.getGithubUrl() == null ? "" : scriptDto.getGithubUrl();
        Boolean isBuildSelected = scriptDto.getIsBuildSelected() != null && scriptDto.getIsBuildSelected();
        Boolean isTestSelected = scriptDto.getIsTestSelected() != null && scriptDto.getIsTestSelected();

        context.put(buildTool.get("buildTool"), buildTool.get("buildTool"));
        context.put("directory", buildTool.get("directory"));
        context.put("isBuildSelected", isBuildSelected);
        context.put("isTestSelected", isTestSelected);
        context.put("branch", branch);
        context.put("githubUrl", githubUrl);

        return context;
    }

    // config 템플릿에 사용되는 context Map 만드는 함수
    public Map<String, Object> buildConfigContext(RequestDto.CreateDto createDto, Script script) {

        Map<String, Object> context = new HashMap<>();

        context.put("description", createDto.getDescription());
        context.put("trigger", createDto.getTrigger());

        if (script != null) {

            String githubUrl = script.getGithubUrl() == null ? "" : script.getGithubUrl();
            String sc = script.getScript() == null ? "" : script.getScript();

            List<String> stringList = scriptEditUtil.extractStageNames(sc);
            String injectedScript = scriptEditUtil.injectBooleanParams(sc);

            context.put("githubUrl", githubUrl);
            context.put("script", injectedScript);
        }

        if (createDto.getSchedule() != null) {

            // 받은 스케줄로 cron식 생성
            String cronExpression = CronExpressionUtil.toCron(createDto.getSchedule());

            context.put("cronExpression", cronExpression);
        }

        return context;
    }

    public Map<String, String> findBuildTool(String repoUrl) {
        String[] parts = parseOwnerAndRepo(repoUrl);
        String owner = parts[0], repo = parts[1];

        // 1) 리포지토리 정보로 default_branch 조회
        String repoApi = String.format("https://api.github.com/repos/%s/%s", owner, repo);
        ResponseEntity<JsonNode> repoResp = restTemplate.getForEntity(repoApi, JsonNode.class);
        if (!repoResp.getStatusCode().is2xxSuccessful() || repoResp.getBody() == null) {
            throw new CustomException(ErrorCode.GIT_CLONE_FAILED);
        }
        String defaultBranch = repoResp.getBody()
                .path("default_branch")
                .asText("main");

        // 2) Git Tree API 호출 (recursive=1)
        String treeApi = String.format(
                "https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1",
                owner, repo, defaultBranch
        );
        ResponseEntity<JsonNode> treeResp = restTemplate.getForEntity(treeApi, JsonNode.class);
        JsonNode tree = Optional.ofNullable(treeResp.getBody())
                .map(b -> b.get("tree"))
                .orElseThrow(() -> new CustomException(ErrorCode.GIT_CLONE_FAILED));

        // 3) path별로 매핑
        String buildTool = "Unknown";
        String scriptDir = ".";  // 디폴트: 루트
        boolean hasPom = false, hasMvnw = false;
        String pomDir = null, mvnwDir = null;

        for (JsonNode node : tree) {
            String path = node.path("path").asText();
            if (path.endsWith("/gradlew") || path.endsWith("/gradlew.bat")) {
                buildTool = "Gradle";
                scriptDir = parentDir(path);
                break;
            }
            if (path.endsWith("pom.xml")) {
                hasPom = true;
                pomDir = parentDir(path);
            }
            if (path.endsWith("/mvnw") || path.endsWith("/mvnw.cmd")) {
                hasMvnw = true;
                mvnwDir = parentDir(path);
            }
            if (path.endsWith("build.gradle") || path.endsWith("build.gradle.kts")) {
                // Gradle 스크립트만 있는 경우
                if (!"Gradle".equals(buildTool)) {
                    buildTool = "Gradle";
                    scriptDir = parentDir(path);
                }
            }
            if (path.endsWith("build.sbt") && "Unknown".equals(buildTool)) {
                buildTool = "SBT";
                scriptDir = parentDir(path);
            }
            if ((path.equals("WORKSPACE") || path.equals("BUILD") || path.equals("BUILD.bazel"))
                    && "Unknown".equals(buildTool)) {
                buildTool = "Bazel";
                scriptDir = parentDir(path);
            }
            if (path.endsWith("build.xml") && "Unknown".equals(buildTool)) {
                buildTool = "Ant";
                scriptDir = parentDir(path);
            }
        }

        // pom + mvnw 조합이면 Maven Wrapper
        if ("Unknown".equals(buildTool) && hasPom) {
            if (hasMvnw) {
                buildTool = "Maven_Wrapper";
                scriptDir = mvnwDir != null ? mvnwDir : pomDir;
            } else {
                buildTool = "Maven";
                scriptDir = pomDir;
            }
        }

        // 최종 디렉터리 보정
        if (scriptDir == null || scriptDir.isEmpty()) {
            scriptDir = ".";
        }

        Map<String, String> result = new HashMap<>();
        result.put("buildTool", buildTool);
        result.put("directory", scriptDir.replace('\\', '/'));
        return result;
    }

    /**
     * path가 "foo/bar/baz.ext" 면 "foo/bar" 반환, 단 루트면 "."
     */
    private String parentDir(String path) {
        int idx = path.lastIndexOf('/');
        return idx > 0 ? path.substring(0, idx) : ".";
    }
}
