package com.subpilot.module.notification.service;

import com.subpilot.module.notification.vo.NotificationPageVO;
import com.subpilot.module.notification.vo.NotificationVO;
import com.subpilot.module.reminder.dto.ReminderEvent;

public interface NotificationService {

    NotificationPageVO list(long page, long size, Boolean readStatus);

    NotificationVO markRead(Long id);

    void markAllRead();

    void createFromReminder(ReminderEvent event);
}
