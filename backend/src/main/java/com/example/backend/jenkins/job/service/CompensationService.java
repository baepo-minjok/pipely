package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Stage;
import com.example.backend.jenkins.job.model.dto.SnapshotRollbackDto;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompensationService {
    private final PipelineRepository pipelineRepository;
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
    public void rollbackLatestVersion(SnapshotRollbackDto dto) {
        Pipeline pipeline = pipelineRepository.findById(dto.getPipelineId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_PIPELINE_NOT_FOUND));
        PipelineVersion version = pipelineVersionRepository.findById(dto.getVersionId())
                .orElseThrow(() -> new CustomException(ErrorCode.VERSION_NOT_FOUND));

        version.setName(dto.getName());
        version.setIsTriggered(dto.isTriggered());
        version.setConfig(dto.getConfig());
        version.setSchedule(dto.getSchedule());
        version.setDescription(dto.getDescription());
        List<Stage> stages = version.getStageList();
        // 마지막에 추가 됐던 stage 삭제
        if (!stages.isEmpty()) {
            stages.remove(stages.size() - 1);
        }
        version.setStageList(stages);
        pipelineVersionRepository.save(version);
        pipeline.setLatestVersionId(version.getId());
        pipelineRepository.save(pipeline);

    }

    public void rollbackPipelineLatestVersion(Pipeline pipeline, UUID previousVersionId) {
        pipeline.setLatestVersionId(previousVersionId);
        pipelineRepository.save(pipeline);
    }
}