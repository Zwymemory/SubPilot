package com.subpilot.module.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Monthly expense trend item")
public record MonthlyTrendVO(
        String month,
        BigDecimal amount
) {
}
