package com.subpilot.module.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Category view object")
public record CategoryVO(
        Long id,
        String name,
        String icon,
        Integer sortOrder,
        Long subscriptionCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
