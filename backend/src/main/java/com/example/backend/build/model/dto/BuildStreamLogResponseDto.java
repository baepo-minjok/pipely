package com.example.backend.build.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.List;

public class BuildStreamLogResponseDto {






    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class BuildStreamLogDto {
        private List<String> log;


        public static BuildStreamLogResponseDto.BuildStreamLogDto getStreamLog(String body) {
            List<String> lines = Arrays.asList(body.split("\\r?\\n"));
            return BuildStreamLogResponseDto.BuildStreamLogDto.builder()
                    .log(lines)
                    .build();
        }



    }



}
