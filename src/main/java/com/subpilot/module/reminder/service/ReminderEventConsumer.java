package com.subpilot.module.reminder.service;

import com.subpilot.infrastructure.rabbitmq.RabbitMqConstants;
import com.subpilot.module.notification.service.NotificationService;
import com.subpilot.module.reminder.dto.ReminderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConstants.REMINDER_QUEUE)
    public void consume(ReminderEvent event) {
        log.info("Consume reminder event: userId={}, type={}", event.userId(), event.reminderType());
        notificationService.createFromReminder(event);
    }
}
