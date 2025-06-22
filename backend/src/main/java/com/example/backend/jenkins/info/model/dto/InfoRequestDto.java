package com.example.backend.jenkins.info.model.dto;

import lombok.Getter;

public class InfoRequestDto {

    @Getter
    public static class CreateDto {

        private String jenkinsId;
        private String uri;
        private String secretKey;


    }
}
