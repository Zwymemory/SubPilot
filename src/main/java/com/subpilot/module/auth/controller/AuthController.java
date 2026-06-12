package com.subpilot.module.auth.controller;

import com.subpilot.common.response.ApiResponse;
import com.subpilot.module.auth.dto.LoginRequest;
import com.subpilot.module.auth.dto.RegisterRequest;
import com.subpilot.module.auth.service.AuthService;
import com.subpilot.module.auth.service.CaptchaService;
import com.subpilot.module.auth.vo.CaptchaVO;
import com.subpilot.module.auth.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @Operation(summary = "Generate login captcha")
    @GetMapping("/captcha")
    public ApiResponse<CaptchaVO> captcha() {
        return ApiResponse.success(captchaService.generate());
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @Operation(summary = "Login with email and password")
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}
