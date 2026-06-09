package com.subpilot.module.subscription.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;
import com.subpilot.infrastructure.redis.CacheService;
import com.subpilot.module.category.entity.CategoryEntity;
import com.subpilot.module.category.service.CategoryService;
import com.subpilot.module.subscription.dto.SubscriptionCreateRequest;
import com.subpilot.module.subscription.dto.SubscriptionUpdateRequest;
import com.subpilot.module.subscription.entity.SubscriptionEntity;
import com.subpilot.module.subscription.enums.SubscriptionStatus;
import com.subpilot.module.subscription.mapper.SubscriptionMapper;
import com.subpilot.module.subscription.service.SubscriptionService;
import com.subpilot.module.subscription.vo.SubscriptionPageVO;
import com.subpilot.module.subscription.vo.SubscriptionVO;
import com.subpilot.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final long MAX_PAGE_SIZE = 100;

    private final SubscriptionMapper subscriptionMapper;
    private final CategoryService categoryService;
    private final CacheService cacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionVO create(SubscriptionCreateRequest request) {
        Long userId = UserContext.getUserId();
        validateCategory(userId, request.categoryId());

        LocalDateTime now = LocalDateTime.now();
        SubscriptionEntity subscription = new SubscriptionEntity();
        fillCreateFields(subscription, userId, request, now);
        subscriptionMapper.insert(subscription);
        cacheService.evictDashboard(userId);
        log.info("Created subscription: userId={}, subscriptionId={}", userId, subscription.getId());
        return toVO(subscription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionVO update(Long id, SubscriptionUpdateRequest request) {
        Long userId = UserContext.getUserId();
        getOwnedEntityOrThrow(userId, id);
        validateCategory(userId, request.categoryId());

        LocalDateTime now = LocalDateTime.now();
        subscriptionMapper.update(null, new LambdaUpdateWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getId, id)
                .eq(SubscriptionEntity::getUserId, userId)
                .set(SubscriptionEntity::getName, request.name().trim())
                .set(SubscriptionEntity::getProvider, trimToNull(request.provider()))
                .set(SubscriptionEntity::getCategoryId, request.categoryId())
                .set(SubscriptionEntity::getDescription, trimToNull(request.description()))
                .set(SubscriptionEntity::getPrice, request.price())
                .set(SubscriptionEntity::getCurrency, request.currency().trim().toUpperCase())
                .set(SubscriptionEntity::getBillingCycle, request.billingCycle().name())
                .set(SubscriptionEntity::getBillingInterval, request.billingInterval())
                .set(SubscriptionEntity::getNextBillingDate, request.nextBillingDate())
                .set(SubscriptionEntity::getExpireDate, request.expireDate())
                .set(SubscriptionEntity::getRemindDaysBefore, request.remindDaysBefore())
                .set(SubscriptionEntity::getAutoRenew, request.autoRenew())
                .set(SubscriptionEntity::getStatus, request.status().name())
                .set(SubscriptionEntity::getWebsite, trimToNull(request.website()))
                .set(SubscriptionEntity::getRemark, trimToNull(request.remark()))
                .set(SubscriptionEntity::getUpdatedAt, now));
        cacheService.evictSubscriptionDetail(userId, id);
        cacheService.evictDashboard(userId);
        log.info("Updated subscription: userId={}, subscriptionId={}", userId, id);
        return toVO(getOwnedEntityOrThrow(userId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        getOwnedEntityOrThrow(userId, id);
        subscriptionMapper.deleteById(id);
        cacheService.evictSubscriptionDetail(userId, id);
        cacheService.evictDashboard(userId);
        log.info("Deleted subscription: userId={}, subscriptionId={}", userId, id);
    }

    @Override
    public SubscriptionVO getDetail(Long id) {
        Long userId = UserContext.getUserId();
        return cacheService.getSubscriptionDetail(userId, id)
                .orElseGet(() -> {
                    SubscriptionVO subscription = toVO(getOwnedEntityOrThrow(userId, id));
                    cacheService.setSubscriptionDetail(userId, id, subscription);
                    return subscription;
                });
    }

    @Override
    public SubscriptionPageVO list(
            long page,
            long size,
            String keyword,
            String status,
            Long categoryId,
            String provider,
            Boolean upcomingOnly
    ) {
        Long userId = UserContext.getUserId();
        long currentPage = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        LambdaQueryWrapper<SubscriptionEntity> wrapper = new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getUserId, userId);
        if (StringUtils.hasText(keyword)) {
            String likeKeyword = keyword.trim();
            wrapper.and(query -> query
                    .like(SubscriptionEntity::getName, likeKeyword)
                    .or().like(SubscriptionEntity::getProvider, likeKeyword)
                    .or().like(SubscriptionEntity::getDescription, likeKeyword)
                    .or().like(SubscriptionEntity::getRemark, likeKeyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SubscriptionEntity::getStatus, parseStatus(status).name());
        }
        if (categoryId != null) {
            wrapper.eq(SubscriptionEntity::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(provider)) {
            wrapper.eq(SubscriptionEntity::getProvider, provider.trim());
        }
        if (Boolean.TRUE.equals(upcomingOnly)) {
            wrapper.apply("next_billing_date IS NOT NULL AND next_billing_date <= DATE_ADD(CURDATE(), INTERVAL remind_days_before DAY)");
        }
        wrapper.orderByAsc(SubscriptionEntity::getNextBillingDate)
                .orderByDesc(SubscriptionEntity::getCreatedAt);

        Page<SubscriptionEntity> result = subscriptionMapper.selectPage(Page.of(currentPage, pageSize), wrapper);
        Map<Long, CategoryEntity> categories = loadCategories(userId, result.getRecords());
        return new SubscriptionPageVO(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords().stream().map(subscription -> toVO(subscription, categories)).toList()
        );
    }

    private void fillCreateFields(
            SubscriptionEntity subscription,
            Long userId,
            SubscriptionCreateRequest request,
            LocalDateTime now
    ) {
        subscription.setUserId(userId);
        subscription.setName(request.name().trim());
        subscription.setProvider(trimToNull(request.provider()));
        subscription.setCategoryId(request.categoryId());
        subscription.setDescription(trimToNull(request.description()));
        subscription.setPrice(request.price());
        subscription.setCurrency(request.currency().trim().toUpperCase());
        subscription.setBillingCycle(request.billingCycle().name());
        subscription.setBillingInterval(request.billingInterval());
        subscription.setNextBillingDate(request.nextBillingDate());
        subscription.setExpireDate(request.expireDate());
        subscription.setRemindDaysBefore(request.remindDaysBefore());
        subscription.setAutoRenew(request.autoRenew());
        subscription.setStatus(request.status().name());
        subscription.setWebsite(trimToNull(request.website()));
        subscription.setRemark(trimToNull(request.remark()));
        subscription.setCreatedAt(now);
        subscription.setUpdatedAt(now);
        subscription.setDeleted(0);
    }

    private SubscriptionEntity getOwnedEntityOrThrow(Long userId, Long subscriptionId) {
        SubscriptionEntity subscription = subscriptionMapper.selectOne(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getId, subscriptionId)
                .eq(SubscriptionEntity::getUserId, userId)
                .last("LIMIT 1"));
        if (subscription == null) {
            cacheService.setEmptySubscriptionDetail(userId, subscriptionId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "订阅不存在");
        }
        return subscription;
    }

    private void validateCategory(Long userId, Long categoryId) {
        if (categoryId != null) {
            categoryService.getOwnedCategoryOrThrow(userId, categoryId);
        }
    }

    private SubscriptionVO toVO(SubscriptionEntity subscription) {
        CategoryEntity category = subscription.getCategoryId() == null
                ? null
                : categoryService.getOwnedCategoryOrThrow(subscription.getUserId(), subscription.getCategoryId());
        return toVO(subscription, category == null ? Map.of() : Map.of(category.getId(), category));
    }

    private SubscriptionVO toVO(SubscriptionEntity subscription, Map<Long, CategoryEntity> categories) {
        CategoryEntity category = subscription.getCategoryId() == null ? null : categories.get(subscription.getCategoryId());
        return new SubscriptionVO(
                subscription.getId(),
                subscription.getName(),
                subscription.getProvider(),
                subscription.getCategoryId(),
                category == null ? null : category.getName(),
                subscription.getDescription(),
                subscription.getPrice(),
                subscription.getCurrency(),
                subscription.getBillingCycle(),
                subscription.getBillingInterval(),
                subscription.getNextBillingDate(),
                subscription.getExpireDate(),
                subscription.getRemindDaysBefore(),
                subscription.getAutoRenew(),
                subscription.getStatus(),
                subscription.getWebsite(),
                subscription.getRemark(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }

    private Map<Long, CategoryEntity> loadCategories(Long userId, java.util.List<SubscriptionEntity> subscriptions) {
        java.util.List<Long> categoryIds = subscriptions.stream()
                .map(SubscriptionEntity::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryIds.stream()
                .map(categoryId -> categoryService.getOwnedCategoryOrThrow(userId, categoryId))
                .collect(Collectors.toMap(CategoryEntity::getId, Function.identity()));
    }

    private SubscriptionStatus parseStatus(String status) {
        try {
            return SubscriptionStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订阅状态不正确");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
