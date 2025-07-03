package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.FreeStyleHistory;
import com.example.backend.jenkins.job.model.dto.FreeStyleRequestDto.CreateFreeStyleDto;
import com.example.backend.jenkins.job.model.dto.FreeStyleRequestDto.UpdateFreeStyleDto;
import com.example.backend.jenkins.job.model.dto.FreeStyleResponseDto.DetailHistoryDto;
import com.example.backend.jenkins.job.model.dto.FreeStyleResponseDto.LightHistoryDto;
import com.example.backend.jenkins.job.repository.FreeStyleHistoryRepository;
import com.example.backend.jenkins.job.repository.FreeStyleRepository;
import com.example.backend.service.HttpClientService;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreeStyleJobService {

    private final MustacheFactory mf;
    private final JenkinsInfoService jenkinsInfoService;
    private final HttpClientService httpClientService;
    private final FreeStyleRepository freeStyleRepository;
    private final FreeStyleHistoryRepository historyRepository;

    @Transactional
    public void createFreestyleJob(CreateFreeStyleDto dto) {

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());

        String jenkinsUrl = info.getUri() + "/createItem?name=" + dto.getJobName();

        String config = createFreestyleConfig(dto);

        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));

        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        registerFreeStyleJob(info, dto, config);

    }

    private String createFreestyleConfig(CreateFreeStyleDto dto) {

        Mustache mustache = null;

        try {
            mustache = mf.compile("template/freeStyleConfig.mustache");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_FILE_NOT_FOUND);
        }
        Map<String, Object> context = new HashMap<>();
        context.put("description", dto.getDescription());
        context.put("projectUrl", dto.getProjectUrl());
        context.put("projectDisplayName", dto.getProjectDisplayName());
        context.put("githubTrigger", dto.getGithubTrigger());
        context.put("repositoryUrl", dto.getRepositoryUrl());
        context.put("branch", dto.getBranch());
        context.put("script", dto.getScript());

        StringWriter writer = new StringWriter();
        try {
            mustache.execute(writer, context).flush();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_EXECUTE_FAILED);
        }

        return writer.toString();
    }

    private void registerFreeStyleJob(JenkinsInfo info, CreateFreeStyleDto dto, String config) {

        FreeStyle job = FreeStyle.builder()
                .jenkinsInfo(info)
                .jobName(dto.getJobName())
                .description(dto.getDescription())
                .projectUrl(dto.getProjectUrl())
                .projectDisplayName(dto.getProjectDisplayName())
                .githubTrigger(dto.getGithubTrigger())
                .repositoryUrl(dto.getRepositoryUrl())
                .branch(dto.getBranch())
                .script(dto.getScript())
                .build();
        freeStyleRepository.save(job);
        info.getFreeStyleList().add(job);

        Integer maxVersion = historyRepository.findMaxVersionByFreeStyle(job);
        int nextVersion = (maxVersion == null ? 1 : maxVersion + 1);

        FreeStyleHistory history = FreeStyleHistory.toHistory(job, nextVersion, config);
    }

    @Transactional
    public void updateFreestyleJob(UpdateFreeStyleDto dto) {

        JenkinsInfo info = getJenkinsInfoByFreeStyleId(dto.getFreeStyleId());
        FreeStyle existing = getFreeStyleById(dto.getFreeStyleId());

        // 1. Fetch original config.xml
        String configUrl = info.getUri() + "/job/" + dto.getJobName() + "/config.xml";
        HttpEntity<Void> getReq = new HttpEntity<>(httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));
        String originalXml = httpClientService.exchange(configUrl, HttpMethod.GET, getReq, String.class);

        // 2. Generate patch fragment via Mustache
        String patchXml = createPatchFragment(dto);
        log.info("Patch XML:\n{}", patchXml);

        // 3. Merge only controlled sections
        String mergedXml = mergeControlledSections(originalXml, patchXml);
        log.info("Merged XML:\n{}", mergedXml);

        // 4. Push updated config to Jenkins
        HttpEntity<String> postReq = new HttpEntity<>(mergedXml, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));
        httpClientService.exchange(configUrl, HttpMethod.POST, postReq, String.class);

        // 5. Persist changed fields in DB
        existing.setDescription(dto.getDescription());
        existing.setProjectUrl(dto.getProjectUrl());
        existing.setProjectDisplayName(dto.getProjectDisplayName());
        existing.setGithubTrigger(dto.getGithubTrigger());
        existing.setRepositoryUrl(dto.getRepositoryUrl());
        existing.setBranch(dto.getBranch());
        existing.setScript(dto.getScript());
        freeStyleRepository.save(existing);

        //6. 버전 관리
        Integer maxVersion = historyRepository.findMaxVersionByFreeStyle(existing);
        int nextVersion = (maxVersion == null ? 1 : maxVersion + 1);

        FreeStyleHistory history = FreeStyleHistory.toHistory(existing, nextVersion, mergedXml);

        historyRepository.save(history);
    }

    private String createPatchFragment(UpdateFreeStyleDto dto) {
        try {
            Mustache m = mf.compile("template/freeStylePatch.mustache");
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("description", dto.getDescription());
            ctx.put("projectUrl", dto.getProjectUrl());
            ctx.put("projectDisplayName", dto.getProjectDisplayName());
            ctx.put("githubTrigger", dto.getGithubTrigger());
            ctx.put("repositoryUrl", dto.getRepositoryUrl());
            ctx.put("branch", dto.getBranch());
            ctx.put("script", dto.getScript());
            StringWriter writer = new StringWriter();
            m.execute(writer, ctx).flush();
            // Wrap fragment in a dummy root for XML parsing
            return "<patch>" + writer.toString() + "</patch>";
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_EXECUTE_FAILED);
        }
    }

    private String mergeControlledSections(String originalXml, String patchXml) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document origDoc = db.parse(new InputSource(new StringReader(originalXml)));
            Document patchDoc = db.parse(new InputSource(new StringReader(patchXml)));

            // Controlled tags that we manage via patch
            String[] controlledTags = new String[]{
                    "description",
                    "com.coravy.hudson.plugins.github.GithubProjectProperty",
                    "scm",
                    "com.cloudbees.jenkins.GitHubPushTrigger",
                    "hudson.tasks.Shell"
            };

            // For each controlled tag, replace or remove
            for (String tag : controlledTags) {
                NodeList patchNodes = patchDoc.getElementsByTagName(tag);
                NodeList origNodes = origDoc.getElementsByTagName(tag);

                if (patchNodes.getLength() > 0) {
                    // Replace first occurrence, or append all
                    for (int i = 0; i < patchNodes.getLength(); i++) {
                        Node patchNode = patchNodes.item(i);
                        Node imported = origDoc.importNode(patchNode, true);
                        if (origNodes.getLength() > i) {
                            origNodes.item(i).getParentNode().replaceChild(imported, origNodes.item(i));
                        } else {
                            // append under root
                            origDoc.getDocumentElement().appendChild(imported);
                        }
                    }
                } else {
                    // No patch for this tag -> remove all occurrences
                    for (int i = origNodes.getLength() - 1; i >= 0; i--) {
                        Node node = origNodes.item(i);
                        node.getParentNode().removeChild(node);
                    }
                }
            }

            // Serialize DOM
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter sw = new StringWriter();
            tf.transform(new DOMSource(origDoc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.XML_PARSING_ERROR);
        }
    }

    @Transactional
    public void deleteById(UUID id) {

        FreeStyle freeStyle = getFreeStyleById(id);

        freeStyle.setIsDeleted(true);
        freeStyle.setDeletedAt(LocalDateTime.now());

        freeStyleRepository.save(freeStyle);
    }

    public DetailHistoryDto getFreeStyleHistoryById(UUID id) {

        FreeStyleHistory freeStyleHistory = historyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_FREESTYLE_HISTORY_NOT_FOUND));

        return DetailHistoryDto.toDetailHistoryDto(freeStyleHistory);
    }

    public List<LightHistoryDto> getLightHistory(UUID id) {

        FreeStyle freeStyle = getFreeStyleById(id);

        return freeStyle.getHistoryList().stream()
                .map(history ->
                        LightHistoryDto.builder()
                                .id(history.getId())
                                .version(history.getVersion())
                                .build()
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollBack(UUID id) {

        FreeStyleHistory freeStyleHistory = historyRepository.findAllWithFreeStyleAndJenkinsInfoByFreeStyleHistoryId(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_FREESTYLE_HISTORY_NOT_FOUND));

        FreeStyle freeStyle = freeStyleHistory.getFreeStyle();

        JenkinsInfo jenkinsInfo = freeStyle.getJenkinsInfo();

        String configUrl = jenkinsInfo.getUri() + "/job/" + freeStyleHistory.getJobName() + "/config.xml";


        HttpEntity<String> postReq = new HttpEntity<>(freeStyleHistory.getConfig(), httpClientService.buildHeaders(jenkinsInfo, new MediaType("application", "xml", StandardCharsets.UTF_8)));
        httpClientService.exchange(configUrl, HttpMethod.POST, postReq, String.class);

        FreeStyle rollBack = FreeStyleHistory.toFreeStyle(freeStyleHistory, freeStyle);

        freeStyleRepository.save(rollBack);

    }

    public FreeStyle getFreeStyleById(UUID id) {
        return freeStyleRepository.findFreeStyleById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_FREESTYLE_NOT_FOUND));
    }

    public JenkinsInfo getJenkinsInfoByFreeStyleId(UUID id) {

        FreeStyle freeStyle = getFreeStyleById(id);
        return freeStyle.getJenkinsInfo();

    }

}
