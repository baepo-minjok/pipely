package com.example.backend.auth.user.controller;

import com.example.backend.auth.user.model.dto.PasswordResetDto;
import com.example.backend.auth.user.service.PasswordResetService;
import com.example.backend.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/password-reset/request")
    public ResponseEntity<BaseResponse<String>> requestPasswordReset(
            @RequestBody @Valid PasswordResetDto.PasswordResetRequest req) {

        passwordResetService.createPasswordResetTokenAndSendEmail(req.getEmail());

        return ResponseEntity.ok()
                .body(BaseResponse.success("send password reset email success"));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<BaseResponse<String>> confirmPasswordReset(
            @RequestBody @Valid PasswordResetDto.PasswordResetSubmission req) {
        passwordResetService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(BaseResponse.success("password reset success"));
    }
}
