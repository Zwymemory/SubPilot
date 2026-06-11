package com.subpilot.module.dashboard.controller;

import com.subpilot.common.response.ApiResponse;
import com.subpilot.module.dashboard.service.DashboardService;
import com.subpilot.module.dashboard.vo.CategoryExpenseVO;
import com.subpilot.module.dashboard.vo.DashboardSummaryVO;
import com.subpilot.module.dashboard.vo.MonthlyTrendVO;
import com.subpilot.module.dashboard.vo.TopSubscriptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get dashboard summary")
    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryVO> summary() {
        return ApiResponse.success(dashboardService.summary());
    }

    @Operation(summary = "Get monthly expense trend")
    @GetMapping("/monthly-trend")
    public ApiResponse<List<MonthlyTrendVO>> monthlyTrend() {
        return ApiResponse.success(dashboardService.monthlyTrend());
    }

    @Operation(summary = "Get category expense distribution")
    @GetMapping("/category-expense")
    public ApiResponse<List<CategoryExpenseVO>> categoryExpense() {
        return ApiResponse.success(dashboardService.categoryExpense());
    }

    @Operation(summary = "Get top expensive subscriptions")
    @GetMapping("/top-subscriptions")
    public ApiResponse<List<TopSubscriptionVO>> topSubscriptions() {
        return ApiResponse.success(dashboardService.topSubscriptions());
    }
}
