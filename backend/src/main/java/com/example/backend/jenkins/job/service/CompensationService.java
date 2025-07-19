package com.example.backend.jenkins.job.service;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompensationService {
    private final PipelineRepository pipelineRepository;
    private final StageService stageService;
    private final HttpClientService httpClientService;
    private final PipelineVersionRepository pipelineVersionRepository;

    @Transactional
    public void deletePipeline(UUID pipelineId) {
        pipelineRepository.findById(pipelineId)
                .ifPresent(pipelineRepository::delete);
    }

    @Transactional
    public void softDeletePipeline(Pipeline pipeline, LocalDateTime time, boolean isDelete) {
        pipeline.setDeletedAt(time);
        pipeline.setIsDeleted(isDelete);
        pipelineRepository.save(pipeline);
        pipelineRepository.flush();
    }

    @Transactional
    public void rollback(PipelineVersion pipelineVersion) {

        Script script = pipelineVersion.getScript();
        stageService.updateStages(pipelineVersion, script);
        pipelineVersionRepository.save(pipelineVersion);

    }

    @Transactional
    public void reCreateJob(PipelineVersion pipelineVersion, JenkinsInfo info, String preName) {
        // DB 롤백
        rollback(pipelineVersion);

        // jenkins 서버 롤백
        String url = info.getUri() + "/createItem?name=" + preName;
        HttpEntity<String> req = new HttpEntity<>(
                pipelineVersion.getConfig(),
                httpClientService.buildHeaders(
                        info,
                        new MediaType("application", "xml", StandardCharsets.UTF_8)
                )
        );
        httpClientService.exchange(url, HttpMethod.POST, req, String.class);
    }

    public void rollbackPipelineLatestVersion(Pipeline pipeline, UUID previousVersionId) {
        pipeline.setLatestVersionId(previousVersionId);
        pipelineRepository.save(pipeline);
    }
}