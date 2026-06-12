package com.subpilot.module.search.document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionSearchDocument(
        Long id,
        Long userId,
        Long categoryId,
        String categoryName,
        String name,
        String provider,
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
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
