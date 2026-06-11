package com.subpilot.module.dashboard.service;

import com.subpilot.module.dashboard.vo.CategoryExpenseVO;
import com.subpilot.module.dashboard.vo.DashboardSummaryVO;
import com.subpilot.module.dashboard.vo.MonthlyTrendVO;
import com.subpilot.module.dashboard.vo.TopSubscriptionVO;

import java.util.List;

public interface DashboardService {

    DashboardSummaryVO summary();

    List<MonthlyTrendVO> monthlyTrend();

    List<CategoryExpenseVO> categoryExpense();

    List<TopSubscriptionVO> topSubscriptions();
}
