package com.example.backend.jenkins.job.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.CreateFreeStyleDto;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.UpdateFreeStyleDto;
import com.example.backend.jenkins.job.service.FreeStyleJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/job")
public class JobController {

    private final FreeStyleJobService freeStyleJobService;

    @PostMapping("/create/freeStyle")
    public ResponseEntity<BaseResponse<String>> createFreeStyle(@RequestBody CreateFreeStyleDto dto) {

        freeStyleJobService.createFreestyleJob(dto);

        return ResponseEntity.ok()
                .body(BaseResponse.success("create freestyle success"));
    }

    @PutMapping("/freeStyle")
    public ResponseEntity<BaseResponse<String>> updateFreeStyle(@RequestBody UpdateFreeStyleDto dto) {

        freeStyleJobService.updateFreestyleJob(dto);

        return ResponseEntity.ok()
                .body(BaseResponse.success("update freestyle success"));
    }
}
