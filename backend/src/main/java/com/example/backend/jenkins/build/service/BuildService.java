package com.example.backend.jenkins.build.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.config.XmlConfigParser;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.service.FreeStyleJobService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final JenkinsInfoService jenkinsInfoService;
    private final HttpClientService httpClientService;
    private final FreeStyleJobService freeStyleJobService;

    public ResponseEntity<?> getBuildInfo(String jobName, JobType jobType, UUID freeStyleId) {
        log.info("빌드 정보 요청 - jobName: {}, jobType: {}", jobName, jobType);
        try {
            return switch (jobType) {
                case LATEST -> ResponseEntity.ok(getLastBuildStatus(jobName, freeStyleId));
                case HISTORY -> ResponseEntity.ok(getBuildHistory(jobName, freeStyleId));
            };
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("빌드 정보 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_SERVER_ERROR);
        }
    }

    public void setTrigger(BuildRequestDto.TriggerSettingRequestDto req, Map<String, String> allParams) {
        UUID id = allParams.containsKey("freeStyle") ? UUID.fromString(allParams.get("freeStyle"))
                : allParams.containsKey("pipeLine") ? UUID.fromString(allParams.get("pipeLine"))
                : null;

        if (id == null) throw new CustomException(ErrorCode.JENKINS_JOB_TYPE_FAILED);

        if (allParams.containsKey("freeStyle")) {
            setupFreestyleTrigger(req, id);
        } else {
            triggerPipeline(req, id);
        }
    }

    public void triggerJenkinsBuild(BuildRequestDto.BuildTriggerRequestDto requestDto, UUID freeStyleId) {
        JenkinsInfo info = freeStyleJobService.getJenkinsInfoByFreeStyleId(freeStyleId);
        String triggerUrl = info.getUri() + "/job/" + requestDto.getJobName() + "/buildWithParameters";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        requestDto.getStepToggles().forEach((key, value) ->
                body.add("DO_" + key.toUpperCase(), String.valueOf(value)));

        httpClientService.exchange(triggerUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    public List<BuildResponseDto.BuildInfo> getBuildHistory(String job, UUID freeStyleId) {
        String response = JenkinsGetResponse(job, freeStyleId);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.listFrom(body);
        } catch (JsonProcessingException e) {
            log.error("빌드 이력 JSON 파싱 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_BUILD_HISTORY_PARSE_ERROR);
        }
    }

    public BuildResponseDto.BuildInfo getLastBuildStatus(String job, UUID freeStyleId) {
        String response = JenkinsGetResponse(job, freeStyleId);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.latestFrom(body);
        } catch (JsonProcessingException e) {
            log.error("최신 빌드 JSON 파싱 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_LATEST_BUILD_PARSE_ERROR);
        }
    }

    public BuildResponseDto.BuildLogDto getBuildLog(String jobName, String buildNumber, UUID freeStyleId) {
        JenkinsInfo info = freeStyleJobService.getJenkinsInfoByFreeStyleId(freeStyleId);
        String url = info.getUri() + "/job/" + jobName + "/" + buildNumber + "/console";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        try {
            String response = httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            Document doc = Jsoup.parse(response);
            Element pre = doc.selectFirst("pre.console-output");
            return BuildResponseDto.BuildLogDto.getLog(pre);
        } catch (Exception e) {
            log.error("콘솔 로그 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_CONSOLE_LOG_PARSE_ERROR);
        }
    }

    public BuildResponseDto.BuildStreamLogDto getStreamLog(String jobName, String buildNumber, UUID freeStyleId) {
        JenkinsInfo info = freeStyleJobService.getJenkinsInfoByFreeStyleId(freeStyleId);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(info.getUri() + "/job/" + jobName + "/" + buildNumber + "/logText/progressiveText")
                .build().toUri();

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        String response = httpClientService.exchange(uri.toString(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        return BuildResponseDto.BuildStreamLogDto.getStreamLog(response);
    }

    public String JenkinsGetResponse(String job, UUID freeStyleId) {
        JenkinsInfo info = freeStyleJobService.getJenkinsInfoByFreeStyleId(freeStyleId);
        String url = info.getUri() + "/job/" + job + "/api/json"
                + "?tree=builds[number,result,timestamp,duration,building,id,url,actions[causes[userId,userName]]]";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    public String getSchedule(String jobName, UUID freeStyleId) {
        JenkinsInfo info = freeStyleJobService.getJenkinsInfoByFreeStyleId(freeStyleId);
        String url = info.getUri() + "/job/" + jobName + "/config.xml";

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED);

        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    public String setSchedule(String jobName, String cron, UUID freeStyleId) {
        JenkinsInfo info = freeStyleJobService.getJenkinsInfoByFreeStyleId(freeStyleId);
        String updatedXml = XmlConfigParser.updateCronSpecInXml(getSchedule(jobName, freeStyleId), cron);

        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_XML);

        httpClientService.exchange(
                info.getUri() + "/job/" + jobName + "/config.xml",
                HttpMethod.POST,
                new HttpEntity<>(updatedXml, headers),
                String.class
        );

        return XmlConfigParser.getCronSpecFromConfig(getSchedule(jobName, freeStyleId));
    }

    public void setupFreestyleTrigger(BuildRequestDto.TriggerSettingRequestDto req, UUID freeStyleId) {
        JenkinsInfo info = freeStyleJobService.getJenkinsInfoByFreeStyleId(freeStyleId);
        String configUrl = info.getUri() + "/job/" + req.getJobName() + "/config.xml";


        HttpHeaders headers = httpClientService.buildHeaders(info, MediaType.APPLICATION_XML);
        headers.setAccept(List.of(MediaType.APPLICATION_XML));

        String xml = httpClientService.exchange(configUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        xml = injectShellScriptBlock(injectParameterBlock(resetBuilderBlock(removeOldParametersBlock(xml)), req.getSteps()), req.getSteps());

        httpClientService.exchange(configUrl, HttpMethod.POST, new HttpEntity<>(xml, headers), String.class);
    }

    public void triggerPipeline(BuildRequestDto.TriggerSettingRequestDto req, UUID id) {
        // TODO: 파이프라인 트리거 로직 구현 필요
    }

    private String injectParameterBlock(String xml, List<String> steps) {
        if (xml.contains("<properties>") && xml.contains("</properties>")) {
            StringBuilder paramBlock = new StringBuilder();
            paramBlock.append("<hudson.model.ParametersDefinitionProperty>\n")
                    .append("    <parameterDefinitions>\n");

            for (String step : steps) {
                paramBlock.append("        <hudson.model.BooleanParameterDefinition>\n")
                        .append("            <name>DO_").append(step.toUpperCase()).append("</name>\n")
                        .append("            <defaultValue>true</defaultValue>\n")
                        .append("            <description>").append(step).append(" step toggle</description>\n")
                        .append("        </hudson.model.BooleanParameterDefinition>\n");
            }

            paramBlock.append("    </parameterDefinitions>\n")
                    .append("</hudson.model.ParametersDefinitionProperty>\n");

            return xml.replaceFirst("<properties>\\s*</properties>",
                    "<properties>" + paramBlock + "</properties>");
        }
        return xml;
    }

    private String injectShellScriptBlock(String xml, List<String> steps) {
        if (xml.contains("<builders>") && xml.contains("</builders>")) {
            StringBuilder shellBlock = new StringBuilder();
            shellBlock.append("<hudson.tasks.Shell>\n")
                    .append("  <command><![CDATA[\n")
                    .append("#!/bin/bash\n\n");

            for (String step : steps) {
                String upper = step.toUpperCase();
                shellBlock.append("if [ \"$DO_").append(upper).append("\" = \"true\" ]; then\n")
                        .append("  echo \"[").append(upper).append("] step is running...\"\n")
                        .append("  sleep 2\n")
                        .append("fi\n\n");
            }

            shellBlock.append("]]></command>\n")
                    .append("</hudson.tasks.Shell>\n");

            return xml.replaceFirst("<builders>\\s*</builders>",
                    Matcher.quoteReplacement("<builders>" + shellBlock + "</builders>"));
        }
        return xml;
    }

    private String resetBuilderBlock(String xml) {
        return xml.replaceAll("<builders>.*?</builders>", Matcher.quoteReplacement("<builders></builders>"));
    }

    private String removeOldParametersBlock(String xml) {
        return xml.replaceAll("<hudson.model.ParametersDefinitionProperty>.*?</hudson.model.ParametersDefinitionProperty>", "");
    }
}
