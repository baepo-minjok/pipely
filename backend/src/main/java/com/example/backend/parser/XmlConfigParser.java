package com.example.backend.parser;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

@Slf4j
public class XmlConfigParser {

    //TODO : 일단 freestyle 기준으로만 트리거 설정들인데 공용적으로 pipeline 테이블 생기면 바꿔야함

    public static String getCronSpecFromConfig(String configXml) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(configXml)));

            NodeList triggers = doc.getElementsByTagName("hudson.triggers.TimerTrigger");
            if (triggers.getLength() > 0) {
                Element trigger = (Element) triggers.item(0);
                NodeList specList = trigger.getElementsByTagName("spec");
                if (specList.getLength() > 0) {
                    String spec = specList.item(0).getTextContent();
                    if (spec != null && !spec.trim().isEmpty()) {
                        log.info("\u23f0 cron spec: '{}'", spec);
                        return spec.trim();
                    } else {
                        log.info("\u2757 cron spec 태그는 존재하지만 내용이 비어있음");
                        return "없음";
                    }
                } else {
                    log.info("\u2757 spec 태그 자체가 존재하지 않음");
                    return "없음";
                }
            } else {
                log.info("\u2757 TimerTrigger 자체가 없음");
                return "없음";
            }
        } catch (Exception e) {
            log.error("XML 파싱 실패", e);
            return "파싱 실패";
        }
    }

    public static String updateCronSpecInXml(String originalXml, String newSpec) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(originalXml)));

            // Remove duplicate PipelineTriggersJobProperty
            NodeList jobProps = doc.getElementsByTagName("org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty");
            if (jobProps.getLength() > 1) {
                for (int i = jobProps.getLength() - 1; i >= 0; i--) {
                    Element prop = (Element) jobProps.item(i);
                    NodeList timers = prop.getElementsByTagName("hudson.triggers.TimerTrigger");
                    if (timers.getLength() == 0) {
                        prop.getParentNode().removeChild(prop);
                        log.info("🗑 중복된 빈 PipelineTriggersJobProperty 제거");
                    }
                }
            }

            NodeList timerTriggers = doc.getElementsByTagName("hudson.triggers.TimerTrigger");

            if (timerTriggers.getLength() > 0) {
                Element trigger = (Element) timerTriggers.item(0);
                NodeList specList = trigger.getElementsByTagName("spec");

                if (specList.getLength() > 0) {
                    specList.item(0).setTextContent(newSpec);
                    log.info("기존 cron spec 업데이트: {}", newSpec);
                } else {
                    Element spec = doc.createElement("spec");
                    spec.setTextContent(newSpec);
                    trigger.appendChild(spec);
                    log.info("기존 TimerTrigger에 spec 추가: {}", newSpec);
                }

            } else {
                NodeList triggersList = doc.getElementsByTagName("triggers");
                Element triggersElem;

                if (triggersList.getLength() > 0) {
                    triggersElem = (Element) triggersList.item(0);
                    log.info("기존 빈 <triggers/> 태그에 TimerTrigger 추가");
                } else {
                    triggersElem = doc.createElement("triggers");
                    Element pipelineProp = doc.createElement("org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty");
                    pipelineProp.appendChild(triggersElem);
                    Node properties = doc.getElementsByTagName("properties").item(0);
                    properties.appendChild(pipelineProp);
                    log.info("<triggers> 및 <PipelineTriggersJobProperty> 생성");
                }

                Element timerTrigger = doc.createElement("hudson.triggers.TimerTrigger");
                Element spec = doc.createElement("spec");
                spec.setTextContent(newSpec);
                timerTrigger.appendChild(spec);
                triggersElem.appendChild(timerTrigger);
                log.info("TimerTrigger + spec 추가: {}", newSpec);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.toString();

        } catch (Exception e) {
            log.error("CRON 스케줄 XML 수정 실패", e);
            throw new RuntimeException("CRON 스케줄 XML 수정 실패", e);
        }
    }
}
