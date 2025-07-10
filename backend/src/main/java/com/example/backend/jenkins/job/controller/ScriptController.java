package com.example.backend.jenkins.job.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.ScriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jenkins/job/script")
public class ScriptController {

    private final ScriptService scriptService;

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<ResponseDto.LightScriptDto>> generateScript(
            @RequestBody @Valid RequestDto.ScriptBaseDto requestDto
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(scriptService.generateScript(requestDto)));
    }


}
