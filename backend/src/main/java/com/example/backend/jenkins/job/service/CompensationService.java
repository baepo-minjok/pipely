package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompensationService {
    private final PipelineRepository pipelineRepository;

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
    public void rollbackLatestVersion(Pipeline pipeline) {
        Integer latest = pipeline.getLatestVersion();

        // 최신 버전 찾기
        PipelineVersion latestVersion = pipeline.getVersionList().stream()
                .filter(v -> v.getVersion().equals(latest))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.VERSION_NOT_FOUND));


            // 리스트에서 제거 -> 매핑되어있는 파이프라인도 삭제됨
            pipeline.getVersionList().remove(latestVersion);

            // 이전버전 중 가장 최신버전
            PipelineVersion target = pipeline.getVersionList().stream()
                    .filter(v -> v.getVersion() < latest)
                    .max(Comparator.comparingInt(PipelineVersion::getVersion))
                    .orElseThrow(() -> new CustomException(ErrorCode.NO_PREVIOUS_VERSION));



            pipeline.setLatestVersion(target.getVersion());
            pipelineRepository.save(pipeline);

    }
}