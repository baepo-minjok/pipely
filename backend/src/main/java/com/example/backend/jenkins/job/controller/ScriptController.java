package com.example.backend.jenkins.job.controller;

import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.ScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Jenkins Script API", description = "Jenkins Script 생성, 삭제, 검증 기능을 제공합니다.")
@RestController
@RequestMapping("/api/jenkins/job/script")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    @Operation(
            summary = "Script 생성 및 반환",
            description = """
                        전달받은 파라미터로 Jenkins Pipeline용 Script를 생성하고,
                        생성된 Script 정보(LightScriptDto)를 반환합니다.
                        - scriptId가 있으면 기존 Script를 수정/재생성합니다.
                        - 없으면 새 Script를 생성합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Script 생성 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류 또는 Script 생성 실패")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Script 생성에 필요한 파라미터"
    )
    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<ResponseDto.LightScriptDto>> generateScript(
            @RequestBody @Valid RequestDto.ScriptBaseDto requestDto
    ) {
        return ResponseEntity.ok()
                .body(BaseResponse.success(scriptService.generateScript(requestDto)));
    }

    @Operation(
            summary = "Script 삭제",
            description = """
                        ScriptId를 이용해 Jenkins Script를 삭제합니다.
                        - Script가 존재하지 않으면 404 에러를 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Script 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Script ID")
    })
    @DeleteMapping
    public ResponseEntity<BaseResponse<String>> deleteScript(
            @Parameter(description = "삭제할 Script의 UUID", required = true, example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e")
            @RequestParam UUID scriptId
    ) {
        scriptService.deleteScript(scriptId);

        return ResponseEntity.ok()
                .body(BaseResponse.success("Script deleted success"));
    }

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
