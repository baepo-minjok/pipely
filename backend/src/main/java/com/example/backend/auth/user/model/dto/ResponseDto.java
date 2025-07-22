package com.example.backend.auth.user.model.dto;

import com.example.backend.jenkins.info.model.dto.InfoResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ResponseDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class detailDto {

        @Schema(description = "사용자 이메일 (로그인 및 인증에 사용됩니다)",
                example = "user@example.com")
        private String email;

        @Schema(description = "사용자 이름 (실명)",
                example = "홍길동")
        private String name;

        @Schema(description = "휴대폰 번호 (연락처 정보)",
                example = "010-1234-5678")
        private String phoneNumber;

        @Schema(description = "마지막 로그인 시간 (ISO-8601 형식)",
                example = "2025-07-01T12:34:56")
        private LocalDateTime lastLogin;

        @Schema(description = "계정 인증 상태",
                example = "true")
        private boolean isVerified;

        private List<InfoResponseDto.LightInfoDto> infoDtoList;
    }
}
