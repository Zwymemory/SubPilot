package com.subpilot.module.reminder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reminder_records")
public class ReminderRecordEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long subscriptionId;

    private Long billId;

    private String reminderType;

    private LocalDate reminderDate;

    private LocalDateTime createdAt;
}
