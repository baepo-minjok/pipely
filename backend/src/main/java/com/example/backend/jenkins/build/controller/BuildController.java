package com.example.backend.jenkins.build.controller;

import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.BuildLogResponseDto;
import com.example.backend.jenkins.build.model.dto.BuildStreamLogResponseDto;
import com.example.backend.jenkins.build.model.dto.BuildTriggerRequestDto;
import com.example.backend.jenkins.build.model.dto.TriggerSettingRequestDto;
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


    @PostMapping("/trigger")
    public ResponseEntity trigger(@RequestBody BuildTriggerRequestDto requestDto, @RequestParam UUID freeStyle) {


        buildService.triggerJenkinsBuild(requestDto, freeStyle);

        return ResponseEntity.ok("");

    }

    @PostMapping("/triggersetting")
    public ResponseEntity<String> triggerSetting(@RequestBody TriggerSettingRequestDto req, @RequestParam Map<String, String> id) {

        buildService.setTrigger(req, id);

        return ResponseEntity.ok("");
    }


    @GetMapping("/schedule")
    public ResponseEntity<String> scheduleJob(@RequestParam String jobName, @RequestParam UUID freeStyle) {


        return ResponseEntity.ok(buildService.getSchedule(jobName, freeStyle));
    }

    @PostMapping("/setSchedule")
    public ResponseEntity<String> getscheduleJob(@RequestParam String jobName, @RequestParam String cron, @RequestParam UUID freeStyle) {


        return ResponseEntity.ok(buildService.setSchedule(jobName, cron, freeStyle));
    }

    @GetMapping("/builds")
    public ResponseEntity<?> getBuilds(@RequestParam String jobName, @RequestParam JobType jobType, UUID freeStyle

    ) {
        return ResponseEntity.ok(buildService.getBuildInfo(jobName, jobType, freeStyle));
    }


    // 4. 빌드 상세 로그 조회
    @GetMapping("/log")
    public ResponseEntity<BuildLogResponseDto.BuildLogDto> getBuildLog(@RequestParam("jobName") String jobName, @RequestParam("buildNumber") String buildNumber, @RequestParam UUID freeStyle

    ) {
        return ResponseEntity.ok(buildService.getBuildLog(jobName, buildNumber, freeStyle));
    }


    // 로그 조회
    @GetMapping(value = "/streamlog")
    public ResponseEntity<BuildStreamLogResponseDto.BuildStreamLogDto> streamLog(@RequestParam("jobName") String jobName, @RequestParam UUID freeStyle, @RequestParam("buildNumber") String buildNumber) {


        return ResponseEntity.ok(buildService.getStreamLog(jobName, buildNumber, freeStyle));


    }


}
