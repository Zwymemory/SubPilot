package com.subpilot.module.reminder.dto;

import com.subpilot.module.reminder.enums.ReminderType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Reminder event message")
public record ReminderEvent(
        Long userId,
        Long subscriptionId,
        Long billId,
        ReminderType reminderType,
        LocalDate reminderDate,
        LocalDate targetDate,
        String subscriptionName,
        BigDecimal amount,
        String currency
) {
}
