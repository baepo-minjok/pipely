package com.example.backend.jenkins.info.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

public class InfoRequestDto {

    @Getter
    @Schema(name = "CreateDto", description = "새로운 Jenkins 정보 등록을 위한 요청 데이터")
    public static class CreateDto {

        @NotBlank
        @Schema(
                description = "Jenkins 연결 이름",
                example = "My CI Server",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String name;

        @Schema(
                description = "Jenkins 서버 설명",
                example = "회사 내부 CI 서버입니다",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String description;

        @NotBlank
        @Schema(
                description = "Jenkins 사용자 아이디",
                example = "admin_user",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String jenkinsId;

        @NotBlank
        @Schema(
                description = "Jenkins 서버 URI",
                example = "https://jenkins.example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String uri;

        @NotBlank
        @Size(min = 8)
        @Schema(
                description = "Jenkins API 토큰 (최소 8자 이상)",
                example = "abcd1234token",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String apiToken;
    }

    @Getter
    @Schema(name = "UpdateDto", description = "기존 Jenkins 정보 수정 요청 DTO")
    public static class UpdateDto {

        @NotNull
        @Schema(
                description = "수정 대상 Jenkins 정보의 ID",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private UUID infoId;

        @NotBlank
        @Schema(
                description = "Jenkins 연결 이름",
                example = "Updated CI Server",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String name;

        @Schema(
                description = "Jenkins 연결 설명",
                example = "업데이트된 서버 설명",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String description;

        @NotBlank
        @Schema(
                description = "Jenkins 사용자 ID",
                example = "jenkins_admin",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String jenkinsId;

        @NotBlank
        @Schema(
                description = "Jenkins 서버 URI",
                example = "https://jenkins.example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String uri;

        @NotBlank
        @Size(min = 8)
        @Schema(
                description = "Jenkins API 토큰",
                example = "n2swfhn23sdf234513aff34",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String apiToken;
    }

    @Getter
    @Schema(name = "InfoDto", description = "Jenkins 상세 조회 또는 검증 요청 DTO")
    public static class InfoDto {

        @NotNull
        @Schema(
                description = "Jenkins 정보의 고유 ID",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private UUID infoId;
    }
}
