package com.subpilot.module.subscription.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Subscription view object")
public record SubscriptionVO(
        Long id,
        String name,
        String provider,
        Long categoryId,
        String categoryName,
        String description,
        BigDecimal price,
        String currency,
        String billingCycle,
        Integer billingInterval,
        LocalDate nextBillingDate,
        LocalDate expireDate,
        Integer remindDaysBefore,
        Boolean autoRenew,
        String status,
        String website,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
