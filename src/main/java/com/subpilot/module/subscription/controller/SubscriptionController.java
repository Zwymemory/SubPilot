package com.subpilot.module.subscription.controller;

import com.subpilot.common.response.ApiResponse;
import com.subpilot.module.subscription.dto.SubscriptionCreateRequest;
import com.subpilot.module.subscription.dto.SubscriptionUpdateRequest;
import com.subpilot.module.subscription.service.SubscriptionService;
import com.subpilot.module.subscription.vo.SubscriptionPageVO;
import com.subpilot.module.subscription.vo.SubscriptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Subscriptions")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Create a subscription")
    @PostMapping
    public ApiResponse<SubscriptionVO> create(@Valid @RequestBody SubscriptionCreateRequest request) {
        return ApiResponse.success(subscriptionService.create(request));
    }

    @Operation(summary = "Update a subscription")
    @PutMapping("/{id}")
    public ApiResponse<SubscriptionVO> update(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionUpdateRequest request
    ) {
        return ApiResponse.success(subscriptionService.update(id, request));
    }

    @Operation(summary = "Delete a subscription")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "Get subscription detail")
    @GetMapping("/{id}")
    public ApiResponse<SubscriptionVO> detail(@PathVariable Long id) {
        return ApiResponse.success(subscriptionService.getDetail(id));
    }

    @Operation(summary = "List current user's subscriptions")
    @GetMapping
    public ApiResponse<SubscriptionPageVO> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Boolean upcomingOnly
    ) {
        return ApiResponse.success(subscriptionService.list(page, size, keyword, status, categoryId, provider, upcomingOnly));
    }
}
