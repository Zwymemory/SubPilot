package com.subpilot.module.bill.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Bill view object")
public record BillVO(
        Long id,
        Long subscriptionId,
        String subscriptionName,
        BigDecimal amount,
        String currency,
        LocalDate billDate,
        LocalDate dueDate,
        LocalDateTime paidTime,
        String status,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
