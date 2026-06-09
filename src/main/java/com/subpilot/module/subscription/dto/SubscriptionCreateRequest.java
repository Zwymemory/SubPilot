package com.subpilot.module.subscription.dto;

import com.subpilot.module.subscription.enums.BillingCycle;
import com.subpilot.module.subscription.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Subscription create request")
public record SubscriptionCreateRequest(
        @NotBlank(message = "订阅名称不能为空")
        @Size(max = 128, message = "订阅名称不能超过 128 个字符")
        String name,

        @Size(max = 128, message = "服务商不能超过 128 个字符")
        String provider,

        Long categoryId,

        @Size(max = 512, message = "描述不能超过 512 个字符")
        String description,

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.00", message = "价格不能小于 0")
        BigDecimal price,

        @NotBlank(message = "币种不能为空")
        @Size(max = 16, message = "币种不能超过 16 个字符")
        String currency,

        @NotNull(message = "计费周期不能为空")
        BillingCycle billingCycle,

        @NotNull(message = "计费间隔不能为空")
        @Min(value = 1, message = "计费间隔不能小于 1")
        @Max(value = 999, message = "计费间隔不能大于 999")
        Integer billingInterval,

        LocalDate nextBillingDate,

        LocalDate expireDate,

        @NotNull(message = "提醒提前天数不能为空")
        @Min(value = 0, message = "提醒提前天数不能小于 0")
        @Max(value = 365, message = "提醒提前天数不能大于 365")
        Integer remindDaysBefore,

        @NotNull(message = "是否自动续费不能为空")
        Boolean autoRenew,

        @NotNull(message = "订阅状态不能为空")
        SubscriptionStatus status,

        @Size(max = 512, message = "官网地址不能超过 512 个字符")
        String website,

        String remark
) {
}
