package com.subpilot.module.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Dashboard summary")
public record DashboardSummaryVO(
        BigDecimal monthlyExpense,
        BigDecimal yearlyExpense,
        Long activeSubscriptionCount,
        Long upcomingBillingCount,
        Long expiringSoonCount,
        Long unreadNotificationCount
) {
}
