package com.subpilot.module.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Notification page response")
public record NotificationPageVO(
        long page,
        long size,
        long total,
        long pages,
        List<NotificationVO> records
) {
}
