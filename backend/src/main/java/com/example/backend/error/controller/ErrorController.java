package com.example.backend.error.controller;

import com.example.backend.error.model.dto.FailedBuildDto;
import com.example.backend.error.service.ErrorService;
import com.example.backend.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/error")
public class ErrorController {

    private final ErrorService errorService;

    @GetMapping("/failed-builds")
    public BaseResponse<List<FailedBuildDto>> getFailedBuilds() {
        List<FailedBuildDto> builds = errorService.getFailedBuilds();
        return BaseResponse.success(builds);
    }

    @GetMapping("/recent-builds")
    public BaseResponse<List<FailedBuildDto>> getRecentBuilds() {
        List<FailedBuildDto> builds = errorService.getRecentBuilds();
        return BaseResponse.success(builds);
    }
}

