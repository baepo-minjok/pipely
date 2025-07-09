package com.example.backend.jenkins.job.model.dto;

public class RequestDto {

    public static class CreateDto {

        // job 이름
        private String name;

        // job 설명
        private String description;

        // 연동된 git 주소
        private String githubUrl;

        // git webhook trigger 설정 여부
        private Boolean trigger;

        // Build Stage가 선택되었는지 여부
        private Boolean isBuildSelected;

        // 선택한 Build 도구
        private String buildTool;
    }
}
