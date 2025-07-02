package com.example.backend.jenkins.info.model.dto;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class InfoResponseDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "LightInfoDto", description = "간략한 Jenkins 정보 응답 DTO")
    public static class LightInfoDto {

        @Schema(
                description = "Jenkins 정보의 고유 식별자",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        private UUID id;

        @Schema(
                description = "Jenkins 설정 이름",
                example = "My Jenkins Server"
        )
        private String name;

        @Schema(
                description = "Jenkins 서버 URI",
                example = "http://jenkins.example.com"
        )
        private String uri;

        public static LightInfoDto fromEntity(JenkinsInfo info) {
            return LightInfoDto.builder()
                    .id(info.getId())
                    .name(info.getName())
                    .uri(info.getUri())
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "DetailInfoDto", description = "상세 Jenkins 정보 응답 DTO")
    public static class DetailInfoDto {

        @Schema(
                description = "Jenkins 정보의 고유 식별자",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        private UUID id;

        @Schema(
                description = "Jenkins 설정 이름",
                example = "My Jenkins"
        )
        private String name;

        @Schema(
                description = "Jenkins 설정 설명",
                example = "CI/CD 자동화를 위한 Jenkins 서버입니다."
        )
        private String description;

        @Schema(
                description = "Jenkins 로그인 ID",
                example = "admin"
        )
        private String jenkinsId;

        @Schema(
                description = "Jenkins API 토큰(실제 정보가 아닌 더미 토큰 제공)",
                example = "abcd1234token"
        )
        private String apiToken;

        @Schema(
                description = "Jenkins 서버 URI",
                example = "http://jenkins.example.com"
        )
        private String uri;

        public static DetailInfoDto fromEntity(JenkinsInfo info) {
            return DetailInfoDto.builder()
                    .id(info.getId())
                    .name(info.getName())
                    .description(info.getDescription())
                    .jenkinsId(info.getJenkinsId())
                    .apiToken("JenkinsApiToken")
                    .uri(info.getUri())
                    .build();
        }
    }
}
