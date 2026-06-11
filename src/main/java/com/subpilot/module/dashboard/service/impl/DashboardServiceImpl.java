package com.subpilot.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.subpilot.infrastructure.redis.CacheService;
import com.subpilot.module.bill.entity.BillEntity;
import com.subpilot.module.bill.enums.BillStatus;
import com.subpilot.module.bill.mapper.BillMapper;
import com.subpilot.module.category.entity.CategoryEntity;
import com.subpilot.module.category.mapper.CategoryMapper;
import com.subpilot.module.dashboard.service.DashboardService;
import com.subpilot.module.dashboard.vo.CategoryExpenseVO;
import com.subpilot.module.dashboard.vo.DashboardSummaryVO;
import com.subpilot.module.dashboard.vo.MonthlyTrendVO;
import com.subpilot.module.dashboard.vo.TopSubscriptionVO;
import com.subpilot.module.notification.entity.NotificationEntity;
import com.subpilot.module.notification.mapper.NotificationMapper;
import com.subpilot.module.subscription.entity.SubscriptionEntity;
import com.subpilot.module.subscription.enums.SubscriptionStatus;
import com.subpilot.module.subscription.mapper.SubscriptionMapper;
import com.subpilot.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BillMapper billMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final CategoryMapper categoryMapper;
    private final NotificationMapper notificationMapper;
    private final CacheService cacheService;

    @Override
    public DashboardSummaryVO summary() {
        Long userId = UserContext.getUserId();
        return cacheService.getDashboardSummary(userId).orElseGet(() -> {
            DashboardSummaryVO summary = buildSummary(userId);
            cacheService.setDashboardSummary(userId, summary);
            return summary;
        });
    }

    @Override
    public List<MonthlyTrendVO> monthlyTrend() {
        Long userId = UserContext.getUserId();
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(5);
        LocalDate startDate = firstMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        for (int offset = 0; offset < 6; offset++) {
            amounts.put(firstMonth.plusMonths(offset).toString(), BigDecimal.ZERO);
        }
        paidBillsBetween(userId, startDate, endDate).forEach(bill -> {
            String month = YearMonth.from(bill.getBillDate()).toString();
            amounts.computeIfPresent(month, (key, amount) -> amount.add(bill.getAmount()));
        });
        return amounts.entrySet().stream()
                .map(entry -> new MonthlyTrendVO(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<CategoryExpenseVO> categoryExpense() {
        Long userId = UserContext.getUserId();
        List<BillEntity> paidBills = paidBillsBetween(userId, YearMonth.now().atDay(1), YearMonth.now().atEndOfMonth());
        Map<Long, SubscriptionEntity> subscriptions = loadSubscriptions(userId, paidBills);
        Map<Long, CategoryEntity> categories = loadCategories(userId, subscriptions.values().stream().toList());
        Map<Long, BigDecimal> amountByCategory = new LinkedHashMap<>();

        paidBills.forEach(bill -> {
            SubscriptionEntity subscription = bill.getSubscriptionId() == null ? null : subscriptions.get(bill.getSubscriptionId());
            Long categoryId = subscription == null ? null : subscription.getCategoryId();
            amountByCategory.merge(categoryId, bill.getAmount(), BigDecimal::add);
        });

        return amountByCategory.entrySet().stream()
                .map(entry -> {
                    CategoryEntity category = entry.getKey() == null ? null : categories.get(entry.getKey());
                    return new CategoryExpenseVO(
                            entry.getKey(),
                            category == null ? "未分类" : category.getName(),
                            entry.getValue()
                    );
                })
                .sorted(Comparator.comparing(CategoryExpenseVO::amount).reversed())
                .toList();
    }

    @Override
    public List<TopSubscriptionVO> topSubscriptions() {
        Long userId = UserContext.getUserId();
        return subscriptionMapper.selectList(new LambdaQueryWrapper<SubscriptionEntity>()
                        .eq(SubscriptionEntity::getUserId, userId)
                        .eq(SubscriptionEntity::getStatus, SubscriptionStatus.ACTIVE.name())
                        .orderByDesc(SubscriptionEntity::getPrice)
                        .last("LIMIT 10"))
                .stream()
                .map(subscription -> new TopSubscriptionVO(
                        subscription.getId(),
                        subscription.getName(),
                        subscription.getProvider(),
                        subscription.getPrice(),
                        subscription.getCurrency(),
                        subscription.getBillingCycle()
                ))
                .toList();
    }

    private DashboardSummaryVO buildSummary(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();
        LocalDate yearStart = LocalDate.now().withDayOfYear(1);
        LocalDate yearEnd = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        LocalDate today = LocalDate.now();

        BigDecimal monthlyExpense = sumPaidBills(userId, monthStart, monthEnd);
        BigDecimal yearlyExpense = sumPaidBills(userId, yearStart, yearEnd);
        Long activeSubscriptionCount = subscriptionMapper.selectCount(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getUserId, userId)
                .eq(SubscriptionEntity::getStatus, SubscriptionStatus.ACTIVE.name()));
        Long upcomingBillingCount = subscriptionMapper.selectCount(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getUserId, userId)
                .isNotNull(SubscriptionEntity::getNextBillingDate)
                .apply("next_billing_date <= DATE_ADD(CURDATE(), INTERVAL remind_days_before DAY)"));
        Long expiringSoonCount = subscriptionMapper.selectCount(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getUserId, userId)
                .isNotNull(SubscriptionEntity::getExpireDate)
                .between(SubscriptionEntity::getExpireDate, today, today.plusDays(30)));
        Long unreadNotificationCount = notificationMapper.selectCount(new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getUserId, userId)
                .eq(NotificationEntity::getReadStatus, false));

        return new DashboardSummaryVO(
                monthlyExpense,
                yearlyExpense,
                activeSubscriptionCount,
                upcomingBillingCount,
                expiringSoonCount,
                unreadNotificationCount
        );
    }

    private BigDecimal sumPaidBills(Long userId, LocalDate startDate, LocalDate endDate) {
        return paidBillsBetween(userId, startDate, endDate).stream()
                .map(BillEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<BillEntity> paidBillsBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return billMapper.selectList(new LambdaQueryWrapper<BillEntity>()
                .eq(BillEntity::getUserId, userId)
                .eq(BillEntity::getStatus, BillStatus.PAID.name())
                .between(BillEntity::getBillDate, startDate, endDate));
    }

    private Map<Long, SubscriptionEntity> loadSubscriptions(Long userId, List<BillEntity> bills) {
        List<Long> subscriptionIds = bills.stream()
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

    private Map<Long, CategoryEntity> loadCategories(Long userId, List<SubscriptionEntity> subscriptions) {
        List<Long> categoryIds = subscriptions.stream()
                .map(SubscriptionEntity::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                        .eq(CategoryEntity::getUserId, userId)
                        .in(CategoryEntity::getId, categoryIds))
                .stream()
                .collect(Collectors.toMap(CategoryEntity::getId, Function.identity()));
    }
}
