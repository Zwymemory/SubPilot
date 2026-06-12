package com.subpilot.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;
import com.subpilot.infrastructure.redis.CacheService;
import com.subpilot.module.notification.entity.NotificationEntity;
import com.subpilot.module.notification.mapper.NotificationMapper;
import com.subpilot.module.notification.service.NotificationService;
import com.subpilot.module.notification.vo.NotificationPageVO;
import com.subpilot.module.notification.vo.NotificationVO;
import com.subpilot.module.reminder.dto.ReminderEvent;
import com.subpilot.module.reminder.entity.ReminderRecordEntity;
import com.subpilot.module.reminder.mapper.ReminderRecordMapper;
import com.subpilot.module.reminder.enums.ReminderType;
import com.subpilot.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final long MAX_PAGE_SIZE = 100;

    private final NotificationMapper notificationMapper;
    private final ReminderRecordMapper reminderRecordMapper;
    private final CacheService cacheService;

    @Override
    public NotificationPageVO list(long page, long size, Boolean readStatus) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getUserId, userId);
        if (readStatus != null) {
            wrapper.eq(NotificationEntity::getReadStatus, readStatus);
        }
        wrapper.orderByAsc(NotificationEntity::getReadStatus)
                .orderByDesc(NotificationEntity::getCreatedAt);
        Page<NotificationEntity> result = notificationMapper.selectPage(
                Page.of(Math.max(page, 1), Math.min(Math.max(size, 1), MAX_PAGE_SIZE)),
                wrapper
        );
        return new NotificationPageVO(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords().stream().map(this::toVO).toList()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationVO markRead(Long id) {
        Long userId = UserContext.getUserId();
        NotificationEntity notification = getOwnedNotificationOrThrow(userId, id);
        if (!Boolean.TRUE.equals(notification.getReadStatus())) {
            notificationMapper.update(null, new LambdaUpdateWrapper<NotificationEntity>()
                    .eq(NotificationEntity::getId, id)
                    .eq(NotificationEntity::getUserId, userId)
                    .set(NotificationEntity::getReadStatus, true)
                    .set(NotificationEntity::getUpdatedAt, LocalDateTime.now()));
            cacheService.evictDashboard(userId);
        }
        return toVO(getOwnedNotificationOrThrow(userId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        Long userId = UserContext.getUserId();
        notificationMapper.update(null, new LambdaUpdateWrapper<NotificationEntity>()
                .eq(NotificationEntity::getUserId, userId)
                .eq(NotificationEntity::getReadStatus, false)
                .set(NotificationEntity::getReadStatus, true)
                .set(NotificationEntity::getUpdatedAt, LocalDateTime.now()));
        cacheService.evictDashboard(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFromReminder(ReminderEvent event) {
        if (event == null || event.userId() == null || event.reminderType() == null || event.reminderDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "提醒消息格式不正确");
        }
        if (existsReminderRecord(event)) {
            log.info("Skip duplicate reminder: userId={}, type={}, subscriptionId={}, billId={}, date={}",
                    event.userId(), event.reminderType(), event.subscriptionId(), event.billId(), event.reminderDate());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ReminderRecordEntity record = new ReminderRecordEntity();
        record.setUserId(event.userId());
        record.setSubscriptionId(event.subscriptionId());
        record.setBillId(event.billId());
        record.setReminderType(event.reminderType().name());
        record.setReminderDate(event.reminderDate());
        record.setCreatedAt(now);
        reminderRecordMapper.insert(record);

        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(event.userId());
        notification.setType(event.reminderType().name());
        notification.setTitle(buildTitle(event));
        notification.setContent(buildContent(event));
        notification.setRelatedType(event.billId() == null ? "SUBSCRIPTION" : "BILL");
        notification.setRelatedId(event.billId() == null ? event.subscriptionId() : event.billId());
        notification.setReadStatus(false);
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        notification.setDeleted(0);
        notificationMapper.insert(notification);
        cacheService.evictDashboard(event.userId());
        log.info("Created notification from reminder: userId={}, notificationId={}", event.userId(), notification.getId());
    }

    private boolean existsReminderRecord(ReminderEvent event) {
        LambdaQueryWrapper<ReminderRecordEntity> wrapper = new LambdaQueryWrapper<ReminderRecordEntity>()
                .eq(ReminderRecordEntity::getUserId, event.userId())
                .eq(ReminderRecordEntity::getReminderType, event.reminderType().name())
                .eq(ReminderRecordEntity::getReminderDate, event.reminderDate());
        if (event.subscriptionId() == null) {
            wrapper.isNull(ReminderRecordEntity::getSubscriptionId);
        } else {
            wrapper.eq(ReminderRecordEntity::getSubscriptionId, event.subscriptionId());
        }
        if (event.billId() == null) {
            wrapper.isNull(ReminderRecordEntity::getBillId);
        } else {
            wrapper.eq(ReminderRecordEntity::getBillId, event.billId());
        }
        return reminderRecordMapper.selectCount(wrapper) > 0;
    }

    private NotificationEntity getOwnedNotificationOrThrow(Long userId, Long notificationId) {
        NotificationEntity notification = notificationMapper.selectOne(new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getId, notificationId)
                .eq(NotificationEntity::getUserId, userId)
                .last("LIMIT 1"));
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "通知不存在");
        }
        return notification;
    }

    private String buildTitle(ReminderEvent event) {
        String name = event.subscriptionName() == null ? "订阅" : event.subscriptionName();
        if (event.reminderType() == ReminderType.BILLING_REMINDER) {
            return name + " 即将扣费";
        }
        if (event.reminderType() == ReminderType.EXPIRING_REMINDER) {
            return name + " 即将到期";
        }
        return name + " 账单已逾期";
    }

    private String buildContent(ReminderEvent event) {
        String name = event.subscriptionName() == null ? "该订阅" : event.subscriptionName();
        if (event.reminderType() == ReminderType.BILLING_REMINDER) {
            return "%s 将在 %s 扣费，金额 %s %s".formatted(
                    name,
                    event.targetDate(),
                    event.amount() == null ? "未知" : event.amount(),
                    event.currency() == null ? "" : event.currency()
            ).trim();
        }
        if (event.reminderType() == ReminderType.EXPIRING_REMINDER) {
            return "%s 将在 %s 到期，请及时处理".formatted(name, event.targetDate());
        }
        return "%s 的账单已在 %s 逾期，请尽快确认支付状态".formatted(name, event.targetDate());
    }

    private NotificationVO toVO(NotificationEntity notification) {
        return new NotificationVO(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedType(),
                notification.getRelatedId(),
                notification.getReadStatus(),
                notification.getCreatedAt()
        );
    }
}
