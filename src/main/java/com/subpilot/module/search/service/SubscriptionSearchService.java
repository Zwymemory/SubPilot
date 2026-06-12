package com.subpilot.module.search.service;

import com.subpilot.module.search.document.SubscriptionSearchDocument;
import com.subpilot.module.subscription.entity.SubscriptionEntity;
import com.subpilot.module.subscription.vo.SubscriptionPageVO;

public interface SubscriptionSearchService {

    void ensureSubscriptionIndex();

    void indexSubscription(SubscriptionEntity subscription);

    void deleteSubscription(Long subscriptionId);

    SubscriptionPageVO searchCurrentUserSubscriptions(long page, long size, String keyword);

    long rebuildCurrentUserSubscriptions();

    SubscriptionSearchDocument toDocument(SubscriptionEntity subscription);
}
