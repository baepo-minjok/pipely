package com.example.backend.jenkins.job.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.CreateFreestyleDto;
import com.example.backend.jenkins.job.service.FreestyleJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/job")
public class JobController {

    private final FreestyleJobService freestyleJobService;

    @PostMapping("/create/freestyle")
    public ResponseEntity<BaseResponse<String>> createFreestyle(@RequestBody CreateFreestyleDto dto) {


        freestyleJobService.createFreestyleJob(dto);

        return ResponseEntity.ok()
                .body(BaseResponse.success("create freestyle success"));
    }
}
