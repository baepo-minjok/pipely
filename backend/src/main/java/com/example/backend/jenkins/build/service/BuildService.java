package com.example.backend.jenkins.build.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.config.XmlConfigParser;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.*;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
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

    public ResponseEntity<?> getBuildInfo(String jobName, JobType jobType, UUID freeStyle) {
        log.info("빌드 정보 요청 - jobName: {}, jobType: {}", jobName, jobType);
        try {
            return switch (jobType) {
                case LATEST -> ResponseEntity.ok(getLastBuildStatus(jobName, freeStyle));
                case HISTORY -> ResponseEntity.ok(getBuildHistory(jobName, freeStyle));
            };
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("빌드 정보 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_SERVER_ERROR);
        }
    }

    public void setTrigger(TriggerSettingRequestDto req, Map<String, String> allParams) {


        if (allParams.containsKey("freeStyle")) {
            UUID id = UUID.fromString(allParams.get("freeStyle"));
            setupFreestyleTrigger(req, id);
        } else if (allParams.containsKey("pipeLine")) {
            UUID id = UUID.fromString(allParams.get("pipeLine"));
            triggerPipeline(req, id);
        } else {
//            throw new IllegalArgumentException("");
            throw new CustomException(ErrorCode.JENKINS_JOB_TOPY_FAILED);
        }


    }


    public void triggerJenkinsBuild(BuildTriggerRequestDto requestDto, UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info = jenkinsInfoService.getDetailInfoById(freeStyle);

        String triggerUrl = info.getUri() + "/job/" + requestDto.getJobName() + "/buildWithParameters";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        for (Map.Entry<String, Boolean> entry : requestDto.getStepToggles().entrySet()) {
            String paramName = "DO_" + entry.getKey().toUpperCase();
            String paramValue = String.valueOf(entry.getValue());  // "true" or "false"

            body.add(paramName, paramValue);
        }

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);


        httpClientService.exchange(triggerUrl, HttpMethod.POST, entity, String.class);

    }

    public List<BuildResponseDto.BuildInfo> getBuildHistory(String job, UUID freeStyle) {
        String response = JenkinsGetResponse(job, freeStyle);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.listFrom(body);
        } catch (JsonProcessingException e) {
            log.error("빌드 이력 JSON 파싱 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_BUILD_HISTORY_PARSE_ERROR);
        }
    }

    public BuildResponseDto.BuildInfo getLastBuildStatus(String job, UUID freeStyle) {
        String response = JenkinsGetResponse(job, freeStyle);
        try {
            Map<String, Object> body = new ObjectMapper().readValue(response, Map.class);
            return BuildResponseDto.BuildInfo.latestFrom(body);
        } catch (JsonProcessingException e) {
            log.error("최신 빌드 JSON 파싱 실패 - jobName: {}", job, e);
            throw new CustomException(ErrorCode.JENKINS_LATEST_BUILD_PARSE_ERROR);
        }
    }

    public BuildLogResponseDto.BuildLogDto getBuildLog(String jobName, String buildNumber, UUID freeStyle) {

        InfoResponseDto.DetailInfoDto info = jenkinsInfoService.getDetailInfoById(freeStyle);

        String url = info.getUri() + "/job/" + jobName + "/" + buildNumber + "/console";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());

        try {
            String response = httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            Document doc = Jsoup.parse(response);
            Element pre = doc.selectFirst("pre.console-output");
            return BuildLogResponseDto.BuildLogDto.getLog(pre);
        } catch (Exception e) {
            log.error("콘솔 로그 조회 실패 - jobName: {}", jobName, e);
            throw new CustomException(ErrorCode.JENKINS_CONSOLE_LOG_PARSE_ERROR);
        }
    }

    public BuildStreamLogResponseDto.BuildStreamLogDto getStreamLog(String jobName, String buildNumber, UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info = jenkinsInfoService.getDetailInfoById(freeStyle);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(info.getUri() + "/job/" + jobName + "/" + buildNumber + "/logText/progressiveText")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());

        String response = httpClientService.exchange(uri.toString(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        return BuildStreamLogResponseDto.BuildStreamLogDto.getStreamLog(response);
    }

    public String JenkinsGetResponse(String job, UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info = jenkinsInfoService.getDetailInfoById(freeStyle);

        String url = info.getUri() + "/job/" + job + "/api/json"
                + "?tree=builds[number,result,timestamp,duration,building,id,url,actions[causes[userId,userName]]]";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());

        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    public String getSchedule(String jobName, UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info = jenkinsInfoService.getDetailInfoById(freeStyle);

        String url = info.getUri() + "/job/" + jobName + "/config.xml";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());

        return httpClientService.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    public String setSchedule(String jobName, String cron, UUID freeStyle) {
        InfoResponseDto.DetailInfoDto info = jenkinsInfoService.getDetailInfoById(freeStyle);

        String originalXml = getSchedule(jobName, freeStyle);
        String updatedXml = XmlConfigParser.updateCronSpecInXml(originalXml, cron);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());
        headers.setContentType(MediaType.APPLICATION_XML);

        httpClientService.exchange(
                info.getUri() + "/job/" + jobName + "/config.xml",
                HttpMethod.POST,
                new HttpEntity<>(updatedXml, headers),
                String.class
        );

        String newConfigXml = getSchedule(jobName, freeStyle);
        return XmlConfigParser.getCronSpecFromConfig(newConfigXml);


    }


    /*
     * FreeStyle Job이라면
     * buildwithParameters 실행
     *
     * */
    public void setupFreestyleTrigger(TriggerSettingRequestDto req, UUID jenkinsId) {


        InfoResponseDto.DetailInfoDto info = jenkinsInfoService.getDetailInfoById(jenkinsId);
        String jenkinsUrl = info.getUri();
        String jobName = req.getJobName();
        List<String> steps = req.getSteps(); // ["BUILD", "DEPLOY"]

        String configUrl = jenkinsUrl + "/job/" + jobName + "/config.xml";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getSecretKey());
        headers.setAccept(List.of(MediaType.APPLICATION_XML));

        // 1. config.xml 불러오기
        String response = httpClientService.exchange(
                configUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        String xml = response;
        // 기존 파라미터 삭제
        xml = removeOldParametersBlock(xml);
        // 기존 빌더 삭제
        xml = resetBuilderBlock(xml);

        //  파라미터 삽입
        xml = injectParameterBlock(xml, steps);

        //   실행 스크립트 삽입
        xml = injectShellScriptBlock(xml, steps);

        //  config.xml 다시 업로드
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(xml, headers);
        httpClientService.exchange(configUrl, HttpMethod.POST, entity, String.class);
    }



    /* pipeline 테이블 기반 처리
       pipeline script 존재 여부 확인 → 파라미터 분기 포함 여부 검사
       없으면 config.xml 생성해서 등록
    */

    public void triggerPipeline(TriggerSettingRequestDto req, UUID id) {

    }



    /*
     * freestyle job에서 만약 파라미터값 없다면 넣어주는 함수
     *
     */

    private String injectParameterBlock(String originalXml, List<String> steps) {
        if (originalXml.contains("<properties>") && originalXml.contains("</properties>")) {

            StringBuilder paramBlockBuilder = new StringBuilder();
            paramBlockBuilder.append("<hudson.model.ParametersDefinitionProperty>\n");
            paramBlockBuilder.append("    <parameterDefinitions>\n");

            for (String step : steps) {
                paramBlockBuilder.append("        <hudson.model.BooleanParameterDefinition>\n");
                paramBlockBuilder.append("            <name>DO_").append(step.toUpperCase()).append("</name>\n");
                paramBlockBuilder.append("            <defaultValue>true</defaultValue>\n");
                paramBlockBuilder.append("            <description>").append(step).append("Whether to run the </description>\n");
                paramBlockBuilder.append("        </hudson.model.BooleanParameterDefinition>\n");
            }

            paramBlockBuilder.append("    </parameterDefinitions>\n");
            paramBlockBuilder.append("</hudson.model.ParametersDefinitionProperty>\n");

            return originalXml.replaceFirst(
                    "<properties>\\s*</properties>",
                    "<properties>" + paramBlockBuilder + "</properties>"
            );
        }

        return originalXml;
    }

    private String injectShellScriptBlock(String originalXml, List<String> steps) {
        if (originalXml.contains("<builders>") && originalXml.contains("</builders>")) {

            StringBuilder shellBlock = new StringBuilder();
            shellBlock.append("<hudson.tasks.Shell>\n");
            shellBlock.append("  <command><![CDATA[\n");
            shellBlock.append("#!/bin/bash\n\n");
            for (String step : steps) {
                String upper = step.toUpperCase();
                shellBlock.append("if [ \"$DO_").append(upper).append("\" = \"true\" ]; then\n");
                shellBlock.append("  echo \"[").append(upper).append("] step is running...\"\n");
                shellBlock.append("  sleep 2\n");
                shellBlock.append("fi\n\n");
            }

            shellBlock.append("]]></command>\n");
            shellBlock.append("</hudson.tasks.Shell>\n");

            return originalXml.replaceFirst(
                    "<builders>\\s*</builders>",
                    Matcher.quoteReplacement("<builders>" + shellBlock.toString() + "</builders>")
            );
        }

        return originalXml;
    }


    private String resetBuilderBlock(String xml) {
        return xml.replaceAll(
                "<builders>.*?</builders>",
                Matcher.quoteReplacement("<builders></builders>")
        );
    }

    private String removeOldParametersBlock(String xml) {
        return xml.replaceAll(
                "<hudson.model.ParametersDefinitionProperty>.*?</hudson.model.ParametersDefinitionProperty>",
                ""
        );
    }


}
