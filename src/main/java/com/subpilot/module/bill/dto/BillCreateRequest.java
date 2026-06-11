package com.subpilot.module.bill.dto;

import com.subpilot.module.bill.enums.BillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Bill create request")
public record BillCreateRequest(
        Long subscriptionId,

        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0.00", message = "金额不能小于 0")
        BigDecimal amount,

        @Size(max = 16, message = "币种不能超过 16 个字符")
        String currency,

        @NotNull(message = "账单日期不能为空")
        LocalDate billDate,

        LocalDate dueDate,

        BillStatus status,

        @Size(max = 512, message = "备注不能超过 512 个字符")
        String remark
) {
}
