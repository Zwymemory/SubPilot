package com.subpilot.module.search.controller;

import com.subpilot.common.response.ApiResponse;
import com.subpilot.module.search.service.SubscriptionSearchService;
import com.subpilot.module.subscription.vo.SubscriptionPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SubscriptionSearchService subscriptionSearchService;

    @Operation(summary = "Search current user's subscriptions")
    @GetMapping("/subscriptions")
    public ApiResponse<SubscriptionPageVO> searchSubscriptions(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(subscriptionSearchService.searchCurrentUserSubscriptions(page, size, keyword));
    }

    @Operation(summary = "Rebuild current user's subscription search index")
    @PostMapping("/subscriptions/rebuild")
    public ApiResponse<Long> rebuildSubscriptionIndex() {
        return ApiResponse.success(subscriptionSearchService.rebuildCurrentUserSubscriptions());
    }
}
