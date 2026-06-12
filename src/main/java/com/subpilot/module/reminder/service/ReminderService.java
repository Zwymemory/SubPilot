package com.subpilot.module.reminder.service;

import com.subpilot.module.reminder.dto.ReminderEvent;

public interface ReminderService {

    void scanBillingReminders();

    void scanExpiringReminders();

    void scanOverdueBills();

    void publish(ReminderEvent event);
}
