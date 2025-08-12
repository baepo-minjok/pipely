package com.example.backend.jenkins.job.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ScriptMode",
        description = """
                  스크립트 제공 방식:
                  - GENERATED: 선택 항목(빌드/테스트/배포 옵션 등)에 따라 시스템이 Jenkinsfile을 생성
                  - MANUAL: 사용자가 Jenkinsfile(스크립트) 원문을 직접 입력
                """
)
public enum ScriptMode {
    @Schema(description = "옵션 기반으로 시스템이 Jenkinsfile을 생성")
    GENERATED,

    @Schema(description = "사용자가 Jenkinsfile 원문을 직접 작성/제공")
    MANUAL
}