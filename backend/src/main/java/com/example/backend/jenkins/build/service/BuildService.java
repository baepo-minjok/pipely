package com.example.backend.jenkins.build.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.parser.XmlConfigParser;
import com.example.backend.service.HttpClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final HttpClientService httpClientService;
    private final PipelineService pipelineService;
    private final XmlConfigParser xmlConfigParser;

    public static String injectParameterBlockForPipelineJob(String xml, List<String> stages) {
        // 1. 기존 파라미터 목록 파싱
        Set<String> existingParams = new HashSet<>();
        Matcher matcher = Pattern.compile("<name>(DO_\\w+)</name>").matcher(xml);
        while (matcher.find()) {
            existingParams.add(matcher.group(1));
        }

        // 2. 새 파라미터 XML 생성
        StringBuilder newParamDefs = new StringBuilder();
        for (String stage : stages) {
            String paramName = "DO_" + stage.toUpperCase();
            if (!existingParams.contains(paramName)) {
                newParamDefs.append("        <hudson.model.BooleanParameterDefinition>\n")
                        .append("          <name>").append(paramName).append("</name>\n")
                        .append("          <defaultValue>true</defaultValue>\n")
                        .append("          <description>").append(stage.toLowerCase()).append(" step toggle</description>\n")
                        .append("        </hudson.model.BooleanParameterDefinition>\n");
            }
        }

        // 3. config.xml에 파라미터 블록 병합
        if (newParamDefs.length() > 0) {
            if (xml.contains("<parameterDefinitions>")) {
                xml = xml.replaceFirst("</parameterDefinitions>", newParamDefs + "    </parameterDefinitions>");
            } else if (xml.contains("<properties>") && xml.contains("</properties>")) {
                String paramBlock =
                        "<hudson.model.ParametersDefinitionProperty>\n" +
                                "    <parameterDefinitions>\n" +
                                newParamDefs +
                                "    </parameterDefinitions>\n" +
                                "</hudson.model.ParametersDefinitionProperty>\n";
                xml = xml.replaceFirst("</properties>", paramBlock + "</properties>");
            } else {
                // properties 태그 자체가 없는 경우
                String block =
                        "<properties>\n" +
                                "  <hudson.model.ParametersDefinitionProperty>\n" +
                                "    <parameterDefinitions>\n" +
                                newParamDefs +
                                "    </parameterDefinitions>\n" +
                                "  </hudson.model.ParametersDefinitionProperty>\n" +
                                "</properties>\n";
                xml = xml.replaceFirst("<definition", block + "<definition");
            }
        }

        // 4. <script> 블록 추출 및 수정
        Matcher scriptMatcher = Pattern.compile("<script>(<!\\[CDATA\\[)?(.*?)(\\]\\]>)?</script>", Pattern.DOTALL).matcher(xml);
        if (scriptMatcher.find()) {
            String scriptContent = scriptMatcher.group(2);
            String modifiedScript = injectStageConditions(scriptContent, stages);
            String newScriptTag = "<script>" + modifiedScript + "</script>";
            xml = xml.replace(scriptMatcher.group(0), newScriptTag);
        }

        return xml;
    }

    private static String injectStageConditions(String script, List<String> stages) {
        for (String stage : stages) {
            String stageName = stage.substring(0, 1).toUpperCase() + stage.substring(1).toLowerCase();
            // 정규표현식으로 stage 찾기
            String pattern = "stage\\([\"']" + stageName + "[\"']\\)\\s*\\{";
            String replacement = "stage('" + stageName + "') {\n    when {\n        expression { params.DO_" + stage.toUpperCase() + " }\n    }";
            script = script.replaceFirst(pattern, replacement);
        }
        return script;
    }

    public ResponseEntity<?> getBuildInfo(BuildRequestDto.getBuildHistory dto) {

        Pipeline pipeline = pipelineService.getPipelineById(dto.getPipeLine());
        JenkinsInfo info = pipeline.getJenkinsInfo();

        String jobName = pipeline.getName();
        JobType jobType = dto.getJobType();
        UUID pipelineId = dto.getPipeLine();


        log.info("빌드 정보 요청 - jobName: {}, jobType: {}", pipeline.getName(), jobType);
        try {
            return switch (jobType) {
                case LATEST -> ResponseEntity.ok(getLastBuildStatus(pipelineId));
                case HISTORY -> ResponseEntity.ok(getBuildHistory(pipelineId));
            };
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("빌드 정보 조회 실패 - jobName: {}", pipeline.getName(), e);
            throw new CustomException(ErrorCode.JENKINS_SERVER_ERROR);
        }
    }

    /*
     * 특정 job 의 마지막 build 번호 조회
     * */

    /*
     * 특정 스테이지 실행 freestyle
     * */
    public void StageJenkinsBuild(BuildRequestDto.BuildStageRequestDto dto) {
        Pipeline pipeline = pipelineService.getPipelineById(dto.getPipeLine());
        JenkinsInfo info = pipeline.getJenkinsInfo();

        String triggerUrl = info.getUri() + "/job/" + pipeline.getName() + "/buildWithParameters";
        log.info("Jenkins Trigger URL = {}", triggerUrl);


        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        dto.getStageToggles().forEach((key, value) ->
                body.add("DO_" + key.toUpperCase(), String.valueOf(value)));
        httpClientService.exchange(triggerUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);


    }

    public void stagePipeline1(BuildRequestDto.StageSettingRequestDto dto) {
        Pipeline pipeline = pipelineService.getPipelineById(dto.getPipeLine());
        JenkinsInfo info = pipeline.getJenkinsInfo();
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_XML);

        String xml = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/config.xml",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        List<String> stageNames = xmlConfigParser.getPipelineStageNamesFromXml(xml);
        String updatexml = injectParameterBlockForPipelineJob(xml, stageNames);


        String rs = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/config.xml",
                HttpMethod.POST,
                new HttpEntity<>(updatexml, headers),
                String.class
        );

        log.info(rs);


    }


    /*
     * 특정 job 빌드 내역 조회
     * */
    public List<BuildResponseDto.BuildInfo> getBuildHistory( UUID pipelineId) {
        String response = JenkinsGetResponse(pipelineId);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.listFrom(body);
        } catch (JsonProcessingException e) {
            log.error("빌드 이력 JSON 파싱 실패 - jobName: {}", e);
            throw new CustomException(ErrorCode.JENKINS_BUILD_HISTORY_PARSE_ERROR);
        }
    }

    public BuildResponseDto.BuildInfo getLastBuildStatus(UUID pipelineId) {
        String response = JenkinsGetResponse(pipelineId);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.latestFrom(body);
        } catch (JsonProcessingException e) {
            log.error("최신 빌드 JSON 파싱 실패 - jobName: {}", e);
            throw new CustomException(ErrorCode.JENKINS_LATEST_BUILD_PARSE_ERROR);
        }
    }

    /*
     * job 의 특정 빌드 번호의 빌드 로그 조회
     *
     * */
    public BuildResponseDto.BuildLogDto getBuildLog(BuildRequestDto.GetLogRequestDto dto) {

        Pipeline pipeline = pipelineService.getPipelineById(dto.getPipeLine());
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String url = info.getUri() + "/job/" + pipeline.getName() + "/" + dto.getBuildNumber() + "/console";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        try {
            String response = httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            Document doc = Jsoup.parse(response);
            Element pre = doc.selectFirst("pre.console-output");
            return BuildResponseDto.BuildLogDto.getLog(pre);
        } catch (Exception e) {
            log.error("콘솔 로그 조회 실패 - jobName: {}", pipeline.getName(), e);
            throw new CustomException(ErrorCode.JENKINS_CONSOLE_LOG_PARSE_ERROR);
        }
    }
    /*
     * cron 시간 읽어옴
     * */

    /*
     * 특정 job의 실시간 빌드 조회
     *
     * */
    public BuildResponseDto.BuildStreamLogDto getStreamLog(UUID pipeLine) {


        Pipeline pipeline = pipelineService.getPipelineById(pipeLine);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        // 2. 마지막 빌드 번호 조회
        String lastBuildUri = info.getUri() + "/job/" + pipeline.getName() + "/lastBuild/buildNumber";
        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String lastBuildResponse = httpClientService.exchange(
                lastBuildUri, HttpMethod.GET, entity, String.class
        );

        int lastBuildNumber = Integer.parseInt(lastBuildResponse.trim());

        // 3. progressive 로그 URI 구성
        URI logUri = UriComponentsBuilder
                .fromHttpUrl(info.getUri() + "/job/" + pipeline.getName() + "/" + lastBuildNumber + "/logText/progressiveText")
                .build().toUri();

        // 4. 로그 내용 요청 및 반환
        HttpHeaders logHeaders = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);
        String logResponse = httpClientService.exchange(logUri.toString(), HttpMethod.GET, new HttpEntity<>(logHeaders), String.class);

        return BuildResponseDto.BuildStreamLogDto.getStreamLog(logResponse);
    }




    /*
     *
     * */
    public String JenkinsGetResponse(UUID pipelineId) {
        Pipeline pipeline = pipelineService.getPipelineById(pipelineId);
        JenkinsInfo info = pipeline.getJenkinsInfo();

        String url = info.getUri() + "/job/" + pipeline.getName() + "/api/json"
                + "?tree=builds[number,result,timestamp,duration,building,id,url,actions[causes[userId,userName]]]";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }






    /*
     *
     *
     *
     *  */

    public BuildResponseDto.Stage getJobPipelineStage(UUID pipeLine) {

        Pipeline pipeline = pipelineService.getPipelineById(pipeLine);
        JenkinsInfo info = pipeline.getJenkinsInfo();


        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_XML);

        String xml = httpClientService.exchange(
                info.getUri() + "/job/" + pipeline.getName() + "/config.xml",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        List<String> stageNames = xmlConfigParser.getPipelineStageNamesFromXml(xml);
        return new BuildResponseDto.Stage(stageNames);


    }



}


