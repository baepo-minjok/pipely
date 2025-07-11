package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompensationService {
    private final PipelineRepository pipelineRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deletePipeline(UUID pipelineId) {
        pipelineRepository.findById(pipelineId)
                .ifPresent(pipelineRepository::delete);
        throw new CustomException(ErrorCode.JENKINS_SERVER_ERROR);
    }
}