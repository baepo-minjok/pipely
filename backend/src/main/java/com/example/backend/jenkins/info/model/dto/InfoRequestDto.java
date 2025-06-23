package com.example.backend.jenkins.info.model.dto;

import lombok.Getter;

import java.util.UUID;

public class InfoRequestDto {

    @Getter
    public static class CreateDto {

        private String name;
        private String description;
        private String jenkinsId;
        private String uri;
        private String secretKey;

    }

    @Getter
    public static class UpdateDto {

        private UUID infoId;
        private String name;
        private String description;
        private String jenkinsId;
        private String uri;
        private String secretKey;
    }
}
