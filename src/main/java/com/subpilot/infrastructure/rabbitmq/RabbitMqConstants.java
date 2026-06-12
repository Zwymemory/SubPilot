package com.subpilot.infrastructure.rabbitmq;

public final class RabbitMqConstants {

    public static final String REMINDER_EXCHANGE = "subpilot.reminder.exchange";
    public static final String REMINDER_QUEUE = "subpilot.reminder.queue";
    public static final String ROUTING_KEY_BILLING = "reminder.billing";
    public static final String ROUTING_KEY_EXPIRING = "reminder.expiring";
    public static final String ROUTING_KEY_OVERDUE = "reminder.overdue";

    private RabbitMqConstants() {
    }
}
