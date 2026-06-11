package com.subpilot.module.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Category expense item")
public record CategoryExpenseVO(
        Long categoryId,
        String categoryName,
        BigDecimal amount
) {
}
