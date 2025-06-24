package com.example.backend.auth.user.controller;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.model.dto.ReactivateRequest;
import com.example.backend.auth.user.service.DormantTokenService;
import com.example.backend.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/reactive")
public class DormantController {

    private final DormantTokenService dormantTokenService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<BaseResponse<String>> sendReactiveEmail(
            @RequestParam String email
    ) {
        emailService.sendDormantNotificationEmail(email);
        return ResponseEntity.ok(BaseResponse.success("send reactive email success"));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<String>> reactive(
            @RequestBody ReactivateRequest req
    ) {
        Users user = dormantTokenService.validateAndGetUserByToken(req.getToken());

        dormantTokenService.deleteTokensByUser(user);

        return ResponseEntity.ok()
                .body(BaseResponse.success("reactive success"));
    }
}
