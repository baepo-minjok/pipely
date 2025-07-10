package com.example.backend.jenkins.build.config;

import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.service.FreeStyleJobService;
import com.example.backend.service.HttpClientService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

@Component
@RequiredArgsConstructor
public class JobTriggerConfigurer {







    /*
     * 매개변수 추가
     * */
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


    /*
     * 쉘 스크립트 추가
     * */


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



    /*
     * 쉘 스크립트 초기화
     * */
    private String resetBuilderBlock(String xml) {
        return xml.replaceAll("<builders>.*?</builders>", Matcher.quoteReplacement("<builders></builders>"));
    }

    /*
     *삭제
     */
    private String removeOldParametersBlock(String xml) {
        return xml.replaceAll("<hudson.model.ParametersDefinitionProperty>.*?</hudson.model.ParametersDefinitionProperty>", "");
    }






}
