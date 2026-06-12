package com.subpilot.module.notification.controller;

import com.subpilot.common.response.ApiResponse;
import com.subpilot.module.notification.service.NotificationService;
import com.subpilot.module.notification.vo.NotificationPageVO;
import com.subpilot.module.notification.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notifications")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List notifications")
    @GetMapping
    public ApiResponse<NotificationPageVO> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) Boolean readStatus
    ) {
        return ApiResponse.success(notificationService.list(page, size, readStatus));
    }

    @Operation(summary = "Mark notification as read")
    @PutMapping("/{id}/read")
    public ApiResponse<NotificationVO> markRead(@PathVariable Long id) {
        return ApiResponse.success(notificationService.markRead(id));
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.success();
    }
}
