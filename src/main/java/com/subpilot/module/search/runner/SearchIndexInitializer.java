package com.subpilot.module.search.runner;

import com.subpilot.module.search.service.SubscriptionSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexInitializer implements ApplicationRunner {

    private final SubscriptionSearchService subscriptionSearchService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            subscriptionSearchService.ensureSubscriptionIndex();
        } catch (RuntimeException exception) {
            log.warn("Elasticsearch subscription index is not ready, search endpoints may be unavailable", exception);
        }
    }
}
