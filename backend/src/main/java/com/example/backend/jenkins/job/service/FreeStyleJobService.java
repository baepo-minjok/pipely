package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.CreateFreeStyleDto;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.UpdateFreeStyleDto;
import com.example.backend.jenkins.job.repository.FreeStyleRepository;
import com.example.backend.service.HttpClientService;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreeStyleJobService {

    private final MustacheFactory mf;
    private final JenkinsInfoService jenkinsInfoService;
    private final HttpClientService httpClientService;
    private final FreeStyleRepository freeStyleRepository;

    @Transactional
    public void createFreestyleJob(CreateFreeStyleDto dto) {

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());

        String jenkinsUrl = info.getUri() + "/createItem?name=" + dto.getJobName();
        String username = info.getJenkinsId();
        String apiToken = info.getSecretKey();

        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "xml", StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        String config = createFreestyleConfig(dto);

        HttpEntity<String> requestEntity = new HttpEntity<>(config, headers);

        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        registerFreeStyleJob(info, dto);
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

    private void registerFreeStyleJob(JenkinsInfo info, CreateFreeStyleDto dto) {

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
    }

    @Transactional
    public void updateFreestyleJob(UpdateFreeStyleDto dto) {
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());
        FreeStyle existing = freeStyleRepository.getReferenceById(dto.getFreeStyleId());

        // 1. Fetch original config.xml
        String configUrl = info.getUri() + "/job/" + dto.getJobName() + "/config.xml";
        HttpEntity<Void> getReq = new HttpEntity<>(buildHeaders(info));
        String originalXml = httpClientService.exchange(configUrl, HttpMethod.GET, getReq, String.class);
        log.info("Original XML:\n{}", originalXml);

        // 2. Generate patch fragment via Mustache
        String patchXml = createPatchFragment(dto);
        log.info("Patch XML:\n{}", patchXml);

        // 3. Merge only controlled sections
        String mergedXml = mergeControlledSections(originalXml, patchXml);
        log.info("Merged XML:\n{}", mergedXml);

        // 4. Push updated config to Jenkins
        HttpEntity<String> postReq = new HttpEntity<>(mergedXml, buildHeaders(info));
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
            throw new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND);
        }
    }

    private HttpHeaders buildHeaders(JenkinsInfo info) {
        String auth = info.getJenkinsId() + ":" + info.getSecretKey();
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(new MediaType("application", "xml", StandardCharsets.UTF_8));
        return headers;
    }


}
