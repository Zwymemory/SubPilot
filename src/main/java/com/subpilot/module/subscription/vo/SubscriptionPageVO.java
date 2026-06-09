package com.subpilot.module.subscription.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Subscription page response")
public record SubscriptionPageVO(
        long page,
        long size,
        long total,
        long pages,
        List<SubscriptionVO> records
) {
}
