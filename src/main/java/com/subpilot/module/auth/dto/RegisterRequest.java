package com.subpilot.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Register request")
public record RegisterRequest(
        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱不能为空")
        @Schema(description = "Email", example = "test@example.com")
        String email,

        @NotBlank(message = "昵称不能为空")
        @Size(max = 64, message = "昵称不能超过 64 个字符")
        @Schema(description = "Nickname", example = "Zhu")
        String nickname,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 72, message = "密码长度必须在 6 到 72 位之间")
        @Schema(description = "Password", example = "123456")
        String password
) {
}
