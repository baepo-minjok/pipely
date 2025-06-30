package com.example.backend.jenkins.build.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Element;

import java.util.List;


public class BuildLogResponseDto {




    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class BuildLogDto {
        private List<String> log;


        public static BuildLogDto getLog(Element pre) {
            String rawLog = pre.text();
            List<String> lines = List.of(rawLog.split("\n"));
            return BuildLogDto.builder()
                    .log(lines)
                    .build();
        }



    }







}
