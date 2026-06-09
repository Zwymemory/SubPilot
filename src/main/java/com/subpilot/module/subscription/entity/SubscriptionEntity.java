package com.subpilot.module.subscription.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("subscriptions")
public class SubscriptionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String name;

    private String provider;

    private String description;

    private BigDecimal price;

    private String currency;

    private String billingCycle;

    private Integer billingInterval;

    private LocalDate nextBillingDate;

    private LocalDate expireDate;

    private Integer remindDaysBefore;

    private Boolean autoRenew;

    private String status;

    private String website;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
