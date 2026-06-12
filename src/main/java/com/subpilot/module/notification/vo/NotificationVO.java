package com.subpilot.module.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Notification view object")
public record NotificationVO(
        Long id,
        String type,
        String title,
        String content,
        String relatedType,
        Long relatedId,
        Boolean readStatus,
        LocalDateTime createdAt
) {
}
