package com.example.backend.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    private String email;

    private String password;

}