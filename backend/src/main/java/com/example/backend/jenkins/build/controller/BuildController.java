package com.example.backend.jenkins.build.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.*;
import com.example.backend.jenkins.build.service.BuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/build")
public class BuildController {


    private final BuildService buildService;

    // 특정 스테이지 목록 보기
    @GetMapping("/stage")
    public ResponseEntity<BaseResponse<BuildResponseDto.Stage>> getScript(@RequestParam UUID JobStyleId){



        return ResponseEntity.ok(BaseResponse.success(buildService.getJobPipelineStage(JobStyleId)));


    }

    // 특정 스테이지 실행
    @PostMapping("/stage")
    public ResponseEntity Steps(@RequestBody BuildRequestDto.BuildStageRequestDto requestDto, @RequestParam UUID JobStyleId) {

        buildService.StageFreeStyleJenkinsBuild(requestDto, JobStyleId);

        return ResponseEntity.ok(BaseResponse.success("특정 Steps 실행"));

    }
    // stage 셋팅
    @PostMapping("/stagesetting")
    public ResponseEntity<BaseResponse<String>> triggerSetting(@RequestBody BuildRequestDto.StageSettingRequestDto req, @RequestParam Map<String, String> JobStyleId) {


        buildService.setStage(req, JobStyleId);

        return ResponseEntity.ok(BaseResponse.success("Steps 설정 완료"));
    }


    @GetMapping("/schedule")
    public ResponseEntity<BaseResponse<String>> scheduleJob(@RequestParam String jobName, @RequestParam UUID JobStyleId) {


        return ResponseEntity.ok(BaseResponse.success(buildService.getSchedule(jobName, JobStyleId)));
    }

    @PostMapping("/setSchedule")
    public ResponseEntity<BaseResponse<String>> getscheduleJob(@RequestBody BuildRequestDto.SetScheduleJob req) {
        buildService.setSchedule(req);

        return ResponseEntity.ok(BaseResponse.success("스케줄 설정이 완료되었습니다."));
    }

    @GetMapping("/builds")
    public ResponseEntity<BaseResponse<?>> getBuilds(@RequestParam String jobName, @RequestParam JobType jobType, @RequestParam UUID JobStyleId) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildInfo(jobName, jobType, JobStyleId)));
    }

    @GetMapping("/log")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildLogDto>> getBuildLog(@RequestParam  String jobName, @RequestParam  String buildNumber, @RequestParam UUID JobStyleId

    ) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getBuildLog(jobName, buildNumber, JobStyleId)));
    }


    // 실시간 로그 조회
    @GetMapping(value = "/streamlog")
    public ResponseEntity<BaseResponse<BuildResponseDto.BuildStreamLogDto>> streamLog(@RequestParam  String jobName, @RequestParam UUID JobStyleId, @RequestParam String buildNumber) {
        return ResponseEntity.ok(BaseResponse.success(buildService.getStreamLog(jobName, buildNumber, JobStyleId)));

    }


}
