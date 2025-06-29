package com.example.backend.jenkins.info.model.dto;

import com.example.backend.jenkins.info.model.JenkinsInfo;
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
    public static class LightInfoDto {

        private UUID id;
        private String name;
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
    public static class DetailInfoDto {

        private UUID id;
        private String name;
        private String description;
        private String jenkinsId;
        private String secretKey;
        private String uri;

        public static DetailInfoDto fromEntity(JenkinsInfo info) {
            return DetailInfoDto.builder()
                    .id(info.getId())
                    .name(info.getName())
                    .description(info.getDescription())
                    .jenkinsId(info.getJenkinsId())
                    .secretKey(info.getSecretKey())
                    .uri(info.getUri())
                    .build();
        }
    }
}
