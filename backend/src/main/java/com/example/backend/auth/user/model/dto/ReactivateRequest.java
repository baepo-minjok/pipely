package com.example.backend.auth.user.model.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReactivateRequest {

    @NotBlank
    private String token;
}
