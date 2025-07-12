package com.example.backend.jenkins.job.service;

import com.example.backend.jenkins.job.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StageService {

    private final StageRepository stageRepository;

    public void deleteByPipelineId(UUID pipelineId) {
        stageRepository.deleteByPipelineId(pipelineId);
    }
}
