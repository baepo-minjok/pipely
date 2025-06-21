package com.example.backend.auth.email.controller;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.service.UserService;
import com.example.backend.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/verify-email")
    public ResponseEntity<BaseResponse<String>> verifyEmail(@RequestParam String token) {
        // Token 검증
        Users user = emailService.validateToken(token);

        // User 상태를 ACTIVE로 바꿈
        userService.setUserStatusActive(user);

        return ResponseEntity.ok()
                .body(BaseResponse.success("email verified"));
    }
}
