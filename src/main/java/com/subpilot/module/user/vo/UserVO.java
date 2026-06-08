package com.subpilot.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "User view object")
public record UserVO(
        Long id,
        String email,
        String nickname,
        String avatarUrl,
        String status,
        LocalDateTime createdAt
) {
}
