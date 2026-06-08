package com.subpilot.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Current user update request")
public record UserUpdateRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 64, message = "昵称不能超过 64 个字符")
        @Schema(description = "Nickname", example = "Zhu")
        String nickname,

        @Size(max = 512, message = "头像地址不能超过 512 个字符")
        @Schema(description = "Avatar URL")
        String avatarUrl
) {
}
