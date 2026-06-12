package com.subpilot.module.reminder.scheduler;

import com.subpilot.module.reminder.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderService reminderService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Shanghai")
    public void scanDailySubscriptionReminders() {
        log.info("Start daily subscription reminder scan");
        reminderService.scanBillingReminders();
        reminderService.scanExpiringReminders();
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Shanghai")
    public void scanHourlyOverdueBills() {
        log.info("Start hourly overdue bill reminder scan");
        reminderService.scanOverdueBills();
    }
}
