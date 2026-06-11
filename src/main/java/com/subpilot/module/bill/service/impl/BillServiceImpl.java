package com.subpilot.module.bill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;
import com.subpilot.infrastructure.redis.CacheService;
import com.subpilot.module.bill.dto.BillCreateRequest;
import com.subpilot.module.bill.entity.BillEntity;
import com.subpilot.module.bill.enums.BillStatus;
import com.subpilot.module.bill.mapper.BillMapper;
import com.subpilot.module.bill.service.BillService;
import com.subpilot.module.bill.vo.BillPageVO;
import com.subpilot.module.bill.vo.BillVO;
import com.subpilot.module.subscription.entity.SubscriptionEntity;
import com.subpilot.module.subscription.mapper.SubscriptionMapper;
import com.subpilot.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private static final long MAX_PAGE_SIZE = 100;

    private final BillMapper billMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final CacheService cacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillVO create(BillCreateRequest request) {
        Long userId = UserContext.getUserId();
        SubscriptionEntity subscription = validateOwnedSubscription(userId, request.subscriptionId());
        BillStatus status = request.status() == null ? BillStatus.UNPAID : request.status();
        if (status == BillStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新建账单不能直接为取消状态");
        }

        LocalDateTime now = LocalDateTime.now();
        BillEntity bill = new BillEntity();
        bill.setUserId(userId);
        bill.setSubscriptionId(request.subscriptionId());
        bill.setAmount(request.amount());
        bill.setCurrency(resolveCurrency(request.currency(), subscription));
        bill.setBillDate(request.billDate());
        bill.setDueDate(request.dueDate());
        bill.setPaidTime(status == BillStatus.PAID ? now : null);
        bill.setStatus(status.name());
        bill.setRemark(trimToNull(request.remark()));
        bill.setCreatedAt(now);
        bill.setUpdatedAt(now);
        bill.setDeleted(0);
        billMapper.insert(bill);
        cacheService.evictDashboard(userId);
        log.info("Created bill: userId={}, billId={}", userId, bill.getId());
        return toVO(bill, subscription);
    }

    @Override
    public BillPageVO list(long page, long size, String status, String keyword, LocalDate startDate, LocalDate endDate) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<BillEntity> wrapper = new LambdaQueryWrapper<BillEntity>()
                .eq(BillEntity::getUserId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(BillEntity::getStatus, parseStatus(status).name());
        }
        if (startDate != null) {
            wrapper.ge(BillEntity::getBillDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillEntity::getBillDate, endDate);
        }
        if (StringUtils.hasText(keyword)) {
            java.util.List<Long> subscriptionIds = subscriptionMapper.selectList(new LambdaQueryWrapper<SubscriptionEntity>()
                            .eq(SubscriptionEntity::getUserId, userId)
                            .like(SubscriptionEntity::getName, keyword.trim()))
                    .stream()
                    .map(SubscriptionEntity::getId)
                    .toList();
            if (subscriptionIds.isEmpty()) {
                return new BillPageVO(Math.max(page, 1), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), 0, 0, java.util.List.of());
            }
            wrapper.in(BillEntity::getSubscriptionId, subscriptionIds);
        }
        wrapper.orderByDesc(BillEntity::getBillDate).orderByDesc(BillEntity::getCreatedAt);
        return pageResult(userId, billMapper.selectPage(Page.of(Math.max(page, 1), Math.min(Math.max(size, 1), MAX_PAGE_SIZE)), wrapper));
    }

    @Override
    public BillVO detail(Long id) {
        Long userId = UserContext.getUserId();
        BillEntity bill = getOwnedBillOrThrow(userId, id);
        return toVO(bill, loadSubscription(userId, bill.getSubscriptionId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillVO markPaid(Long id) {
        Long userId = UserContext.getUserId();
        BillEntity bill = getOwnedBillOrThrow(userId, id);
        if (BillStatus.CANCELLED.name().equals(bill.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "已取消账单不能标记为已支付");
        }
        LocalDateTime now = LocalDateTime.now();
        billMapper.update(null, new LambdaUpdateWrapper<BillEntity>()
                .eq(BillEntity::getId, id)
                .eq(BillEntity::getUserId, userId)
                .set(BillEntity::getStatus, BillStatus.PAID.name())
                .set(BillEntity::getPaidTime, now)
                .set(BillEntity::getUpdatedAt, now));
        cacheService.evictDashboard(userId);
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillVO markUnpaid(Long id) {
        Long userId = UserContext.getUserId();
        BillEntity bill = getOwnedBillOrThrow(userId, id);
        if (!BillStatus.PAID.name().equals(bill.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "只有已支付账单可以标记为未支付");
        }
        LocalDateTime now = LocalDateTime.now();
        billMapper.update(null, new LambdaUpdateWrapper<BillEntity>()
                .eq(BillEntity::getId, id)
                .eq(BillEntity::getUserId, userId)
                .set(BillEntity::getStatus, BillStatus.UNPAID.name())
                .set(BillEntity::getPaidTime, null)
                .set(BillEntity::getUpdatedAt, now));
        cacheService.evictDashboard(userId);
        return detail(id);
    }

    @Override
    public BillPageVO listBySubscription(Long subscriptionId, long page, long size) {
        Long userId = UserContext.getUserId();
        validateOwnedSubscription(userId, subscriptionId);
        LambdaQueryWrapper<BillEntity> wrapper = new LambdaQueryWrapper<BillEntity>()
                .eq(BillEntity::getUserId, userId)
                .eq(BillEntity::getSubscriptionId, subscriptionId)
                .orderByDesc(BillEntity::getBillDate)
                .orderByDesc(BillEntity::getCreatedAt);
        return pageResult(userId, billMapper.selectPage(Page.of(Math.max(page, 1), Math.min(Math.max(size, 1), MAX_PAGE_SIZE)), wrapper));
    }

    private BillPageVO pageResult(Long userId, Page<BillEntity> result) {
        Map<Long, SubscriptionEntity> subscriptions = loadSubscriptions(userId, result.getRecords());
        return new BillPageVO(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords().stream()
                        .map(bill -> toVO(bill, bill.getSubscriptionId() == null ? null : subscriptions.get(bill.getSubscriptionId())))
                        .toList()
        );
    }

    private BillEntity getOwnedBillOrThrow(Long userId, Long billId) {
        BillEntity bill = billMapper.selectOne(new LambdaQueryWrapper<BillEntity>()
                .eq(BillEntity::getId, billId)
                .eq(BillEntity::getUserId, userId)
                .last("LIMIT 1"));
        if (bill == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "账单不存在");
        }
        return bill;
    }

    private SubscriptionEntity validateOwnedSubscription(Long userId, Long subscriptionId) {
        if (subscriptionId == null) {
            return null;
        }
        SubscriptionEntity subscription = loadSubscription(userId, subscriptionId);
        if (subscription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订阅不存在");
        }
        return subscription;
    }

    private SubscriptionEntity loadSubscription(Long userId, Long subscriptionId) {
        if (subscriptionId == null) {
            return null;
        }
        return subscriptionMapper.selectOne(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getId, subscriptionId)
                .eq(SubscriptionEntity::getUserId, userId)
                .last("LIMIT 1"));
    }

    private Map<Long, SubscriptionEntity> loadSubscriptions(Long userId, java.util.List<BillEntity> bills) {
        java.util.List<Long> subscriptionIds = bills.stream()
                .map(BillEntity::getSubscriptionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (subscriptionIds.isEmpty()) {
            return Map.of();
        }
        return subscriptionMapper.selectList(new LambdaQueryWrapper<SubscriptionEntity>()
                        .eq(SubscriptionEntity::getUserId, userId)
                        .in(SubscriptionEntity::getId, subscriptionIds))
                .stream()
                .collect(Collectors.toMap(SubscriptionEntity::getId, Function.identity()));
    }

    private BillVO toVO(BillEntity bill, SubscriptionEntity subscription) {
        return new BillVO(
                bill.getId(),
                bill.getSubscriptionId(),
                subscription == null ? null : subscription.getName(),
                bill.getAmount(),
                bill.getCurrency(),
                bill.getBillDate(),
                bill.getDueDate(),
                bill.getPaidTime(),
                bill.getStatus(),
                bill.getRemark(),
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }

    private String resolveCurrency(String currency, SubscriptionEntity subscription) {
        if (StringUtils.hasText(currency)) {
            return currency.trim().toUpperCase();
        }
        if (subscription != null && StringUtils.hasText(subscription.getCurrency())) {
            return subscription.getCurrency();
        }
        return "CNY";
    }

    private BillStatus parseStatus(String status) {
        try {
            return BillStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账单状态不正确");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
