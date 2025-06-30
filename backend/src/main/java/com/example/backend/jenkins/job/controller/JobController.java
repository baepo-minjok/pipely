package com.example.backend.jenkins.job.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.CreateFreeStyleDto;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.DetailHistoryDto;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.LightHistoryDto;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.UpdateFreeStyleDto;
import com.example.backend.jenkins.job.service.FreeStyleJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @DeleteMapping("/freeStyle")
    public ResponseEntity<BaseResponse<String>> deleteFreeStyle(@RequestParam UUID id) {
        freeStyleJobService.deleteById(id);

        return ResponseEntity.ok()
                .body(BaseResponse.success("delete freestyle success"));
    }

    @GetMapping("/freeStyle/history")
    public ResponseEntity<BaseResponse<List<LightHistoryDto>>> getFreeStyleHistory(@RequestParam UUID id) {

        return ResponseEntity.ok()
                .body(BaseResponse.success(freeStyleJobService.getLightHistory(id)));
    }

    @GetMapping("/freeStyle/history/{id}")
    public ResponseEntity<BaseResponse<DetailHistoryDto>> getFreeStyleHistoryById(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(freeStyleJobService.getFreeStyleHistoryById(id)));
    }

    @GetMapping("/freeStyle/rollBack")
    public ResponseEntity<BaseResponse<String>> rollBack(@RequestParam UUID id) {

        freeStyleJobService.rollBack(id);

        return ResponseEntity.ok()
                .body(BaseResponse.success("roll back success"));

    }
}
