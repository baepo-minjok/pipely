package com.example.backend.jenkins.job.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class RequestDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateDto {

        // jenkins 정보 id
        private UUID infoId;

        // job 이름
        private String name;

        // job 설명
        private String description;

        // 연동된 git 주소
        private String githubUrl;

        // clone할 branch 이름
        private String branch;

        // 빌드를 실행할 폴더 경로
        private String directory;

        // git webhook trigger 설정 여부
        private Boolean trigger;

        // Build Stage가 선택되었는지 여부
        private Boolean isBuildSelected;

        // 선택한 Build 도구
        private String buildTool;
    }
}
