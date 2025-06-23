package com.example.backend.jenkins.info.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.CreateDto;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.UpdateDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.DetailInfoDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.LightInfoDto;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jenkins/info")
@RequiredArgsConstructor
public class JenkinsInfoController {

    private final JenkinsInfoService jenkinsInfoService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<JenkinsInfo>> create(
            @AuthenticationPrincipal Users user,
            @RequestBody CreateDto request) {
        JenkinsInfo created = jenkinsInfoService.createJenkinsInfo(user, request);
        return ResponseEntity.ok()
                .body(BaseResponse.success(created));
    }

    @PutMapping
    public ResponseEntity<BaseResponse<String>> update(
            @RequestBody UpdateDto request) {
        jenkinsInfoService.updateJenkinsInfo(request);
        return ResponseEntity.ok()
                .body(BaseResponse.success("Jenkins Info update Success"));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<LightInfoDto>>> getAll(
            @AuthenticationPrincipal Users user
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(jenkinsInfoService.getAllLightDtoByUser(user)));
    }

    @GetMapping("/{infoId}")
    public ResponseEntity<BaseResponse<DetailInfoDto>> getById(
            @PathVariable UUID infoId
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(jenkinsInfoService.getDetailInfoById(infoId)));
    }

    @DeleteMapping("/{infoId}")
    public ResponseEntity<BaseResponse<String>> delete(
            @PathVariable UUID infoId
    ) {
        jenkinsInfoService.deleteJenkinsInfo(infoId);
        return ResponseEntity.ok()
                .body(BaseResponse.success("delete Jenkins Info Success"));
    }
}
