package com.subpilot.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public record LoginRequest(
        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱不能为空")
        @Schema(description = "Email", example = "test@example.com")
        String email,

        @NotBlank(message = "密码不能为空")
        @Schema(description = "Password", example = "123456")
        String password
) {
}
