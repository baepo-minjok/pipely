package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
    public Map<String, Object> buildScriptContext(RequestDto.GenerateScriptDto scriptDto) {

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
    public Map<String, Object> buildConfigContext(RequestDto.CreateDto createDto) {


        String githubUrl = createDto.getGithubUrl() == null ? "" : createDto.getGithubUrl();
        String script = createDto.getScript() == null ? "" : createDto.getScript();

        Map<String, Object> context = new HashMap<>();

        context.put("description", createDto.getDescription());
        context.put("githubUrl", githubUrl);
        context.put("script", script);
        context.put("trigger", createDto.getTrigger());

        return context;
    }

    public Map<String, String> findBuildTool(String repoUrl) {
        String destDir = "cloned_repo";
        Path destPath = Paths.get(destDir);

        String buildTool;
        Path toolDir;
        try {
            if (Files.exists(destPath)) {
                System.out.println("Deleting existing directory: " + destPath.toAbsolutePath());
                Files.walk(destPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }

            System.out.println("Cloning " + repoUrl + "...");
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(destDir))
                    .call();

            Optional<Path> gradlewPath;
            Optional<Path> pomPath;
            Optional<Path> mvnwPath;
            Stream<Path> stream = Files.walk(destPath);
            gradlewPath = stream.filter(p -> p.getFileName().toString().equals("gradlew")
                            || p.getFileName().toString().equalsIgnoreCase("gradlew.bat"))
                    .findFirst();

            stream = Files.walk(destPath);
            pomPath = stream.filter(p -> p.getFileName().toString().equalsIgnoreCase("pom.xml"))
                    .findFirst();

            stream = Files.walk(destPath);
            mvnwPath = stream.filter(p -> p.getFileName().toString().equalsIgnoreCase("mvnw")
                            || p.getFileName().toString().equalsIgnoreCase("mvnw.cmd"))
                    .findFirst();
            if (gradlewPath.isPresent()) {
                buildTool = "gradle";
                toolDir = gradlewPath.get().getParent();
            } else if (pomPath.isPresent() && mvnwPath.isPresent()) {
                buildTool = "maven_wrapper";
                toolDir = mvnwPath.get().getParent();
            } else if (pomPath.isPresent()) {
                buildTool = "maven";
                toolDir = pomPath.get().getParent();
            } else {
                // 지원하지 않는 build tool 사용중
                throw new CustomException(ErrorCode.JENKINS_NOT_SUPPORTED_TOOL);
            }
        } catch (GitAPIException | IOException e) {
            throw new CustomException(ErrorCode.GIT_CLONE_FAILED);
        }

        String directory = destPath.relativize(toolDir).toString().replace('\\', '/');
        if (directory.isEmpty()) {
            directory = ".";
        }


        Map<String, String> map = new HashMap<>();
        map.put("buildTool", buildTool);
        map.put("directory", directory);

        return map;
    }
}
