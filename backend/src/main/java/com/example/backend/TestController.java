package com.example.backend;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/custom-error")
    public ResponseEntity<Void> customError() {
        throw new CustomException(ErrorCode.VALIDATION_FAILED);
    }
}
