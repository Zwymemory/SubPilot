package com.subpilot.module.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Top subscription item")
public record TopSubscriptionVO(
        Long id,
        String name,
        String provider,
        BigDecimal price,
        String currency,
        String billingCycle
) {
}
