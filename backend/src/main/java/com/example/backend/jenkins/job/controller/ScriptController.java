package com.example.backend.jenkins.job.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.service.ScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Jenkins Script API", description = "Jenkins Script 생성, 삭제, 검증 기능을 제공합니다.")
@RestController
@RequestMapping("/api/jenkins/job/script")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;


    @Operation(
            summary = "Script 유효성 검증",
            description = """
                        전달받은 Jenkins Script 문자열의 유효성을 검증합니다.
                        - JenkinsInfoId, script 파라미터를 입력받습니다.
                        - 유효하지 않을 경우 400 에러를 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Script 유효성 검증 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 Jenkins Script")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Jenkins Script 유효성 검증 요청 파라미터"
    )
    @PostMapping("/validate")
    public ResponseEntity<BaseResponse<String>> validateScript(
            @RequestBody @Valid RequestDto.ScriptValidateDto requestDto
    ) {
        scriptService.validateScript(requestDto);

        return ResponseEntity.ok()
                .body(BaseResponse.success("Script validation success"));
    }
}
