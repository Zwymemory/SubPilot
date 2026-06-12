package com.subpilot.module.reminder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.subpilot.infrastructure.rabbitmq.RabbitMqConstants;
import com.subpilot.module.bill.entity.BillEntity;
import com.subpilot.module.bill.enums.BillStatus;
import com.subpilot.module.bill.mapper.BillMapper;
import com.subpilot.module.reminder.dto.ReminderEvent;
import com.subpilot.module.reminder.enums.ReminderType;
import com.subpilot.module.reminder.service.ReminderService;
import com.subpilot.module.subscription.entity.SubscriptionEntity;
import com.subpilot.module.subscription.enums.SubscriptionStatus;
import com.subpilot.module.subscription.mapper.SubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final RabbitTemplate rabbitTemplate;
    private final SubscriptionMapper subscriptionMapper;
    private final BillMapper billMapper;

    @Override
    public void scanBillingReminders() {
        LocalDate today = LocalDate.now();
        subscriptionMapper.selectList(new LambdaQueryWrapper<SubscriptionEntity>()
                        .eq(SubscriptionEntity::getStatus, SubscriptionStatus.ACTIVE.name())
                        .isNotNull(SubscriptionEntity::getNextBillingDate)
                        .apply("next_billing_date <= DATE_ADD(CURDATE(), INTERVAL remind_days_before DAY)"))
                .forEach(subscription -> publish(new ReminderEvent(
                        subscription.getUserId(),
                        subscription.getId(),
                        null,
                        ReminderType.BILLING_REMINDER,
                        today,
                        subscription.getNextBillingDate(),
                        subscription.getName(),
                        subscription.getPrice(),
                        subscription.getCurrency()
                )));
    }

    @Override
    public void scanExpiringReminders() {
        LocalDate today = LocalDate.now();
        subscriptionMapper.selectList(new LambdaQueryWrapper<SubscriptionEntity>()
                        .eq(SubscriptionEntity::getStatus, SubscriptionStatus.ACTIVE.name())
                        .isNotNull(SubscriptionEntity::getExpireDate)
                        .between(SubscriptionEntity::getExpireDate, today, today.plusDays(30)))
                .forEach(subscription -> publish(new ReminderEvent(
                        subscription.getUserId(),
                        subscription.getId(),
                        null,
                        ReminderType.EXPIRING_REMINDER,
                        today,
                        subscription.getExpireDate(),
                        subscription.getName(),
                        null,
                        subscription.getCurrency()
                )));
    }

    @Override
    public void scanOverdueBills() {
        LocalDate today = LocalDate.now();
        billMapper.selectList(new LambdaQueryWrapper<BillEntity>()
                        .in(BillEntity::getStatus, BillStatus.UNPAID.name(), BillStatus.OVERDUE.name())
                        .isNotNull(BillEntity::getDueDate)
                        .lt(BillEntity::getDueDate, today))
                .forEach(bill -> publish(new ReminderEvent(
                        bill.getUserId(),
                        bill.getSubscriptionId(),
                        bill.getId(),
                        ReminderType.OVERDUE_REMINDER,
                        today,
                        bill.getDueDate(),
                        null,
                        bill.getAmount(),
                        bill.getCurrency()
                )));
    }

    @Override
    public void publish(ReminderEvent event) {
        String routingKey = routingKey(event.reminderType());
        try {
            rabbitTemplate.convertAndSend(RabbitMqConstants.REMINDER_EXCHANGE, routingKey, event);
            log.info("Published reminder event: userId={}, type={}, routingKey={}",
                    event.userId(), event.reminderType(), routingKey);
        } catch (AmqpException exception) {
            log.warn("Publish reminder event failed: userId={}, type={}",
                    event.userId(), event.reminderType(), exception);
        }
    }

    private String routingKey(ReminderType reminderType) {
        return switch (reminderType) {
            case BILLING_REMINDER -> RabbitMqConstants.ROUTING_KEY_BILLING;
            case EXPIRING_REMINDER -> RabbitMqConstants.ROUTING_KEY_EXPIRING;
            case OVERDUE_REMINDER -> RabbitMqConstants.ROUTING_KEY_OVERDUE;
        };
    }
}
