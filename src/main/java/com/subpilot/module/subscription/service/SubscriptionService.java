package com.subpilot.module.subscription.service;

import com.subpilot.module.subscription.dto.SubscriptionCreateRequest;
import com.subpilot.module.subscription.dto.SubscriptionUpdateRequest;
import com.subpilot.module.subscription.vo.SubscriptionPageVO;
import com.subpilot.module.subscription.vo.SubscriptionVO;

public interface SubscriptionService {

    SubscriptionVO create(SubscriptionCreateRequest request);

    SubscriptionVO update(Long id, SubscriptionUpdateRequest request);

    void delete(Long id);

    SubscriptionVO getDetail(Long id);

    SubscriptionPageVO list(
            long page,
            long size,
            String keyword,
            String status,
            Long categoryId,
            String provider,
            Boolean upcomingOnly
    );
}
