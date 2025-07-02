package com.example.backend.auth.user.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "계정 재활성화 토큰 dto")
public class ReactivateRequestDto {

    @NotBlank
    @Schema(
            description = "고유 토큰 값(UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;
}
