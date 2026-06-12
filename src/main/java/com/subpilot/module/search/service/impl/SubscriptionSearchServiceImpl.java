package com.subpilot.module.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;
import com.subpilot.module.category.entity.CategoryEntity;
import com.subpilot.module.category.service.CategoryService;
import com.subpilot.module.search.constant.SearchIndexConstants;
import com.subpilot.module.search.document.SubscriptionSearchDocument;
import com.subpilot.module.search.service.SubscriptionSearchService;
import com.subpilot.module.subscription.entity.SubscriptionEntity;
import com.subpilot.module.subscription.mapper.SubscriptionMapper;
import com.subpilot.module.subscription.vo.SubscriptionPageVO;
import com.subpilot.module.subscription.vo.SubscriptionVO;
import com.subpilot.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionSearchServiceImpl implements SubscriptionSearchService {

    private static final long MAX_PAGE_SIZE = 100;

    private final ElasticsearchClient elasticsearchClient;
    private final SubscriptionMapper subscriptionMapper;
    private final CategoryService categoryService;

    @Override
    public void ensureSubscriptionIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(request -> request.index(SearchIndexConstants.SUBSCRIPTION_INDEX))
                    .value();
            if (exists) {
                return;
            }
            elasticsearchClient.indices().create(request -> request
                    .index(SearchIndexConstants.SUBSCRIPTION_INDEX)
                    .settings(settings -> settings
                            .numberOfShards("1")
                            .numberOfReplicas("0"))
                    .mappings(mapping -> mapping
                            .properties("id", property -> property.long_(type -> type))
                            .properties("userId", property -> property.long_(type -> type))
                            .properties("categoryId", property -> property.long_(type -> type))
                            .properties("categoryName", property -> property.text(type -> type
                                    .fields("keyword", field -> field.keyword(keyword -> keyword.ignoreAbove(256)))))
                            .properties("name", property -> property.text(type -> type
                                    .fields("keyword", field -> field.keyword(keyword -> keyword.ignoreAbove(256)))))
                            .properties("provider", property -> property.text(type -> type
                                    .fields("keyword", field -> field.keyword(keyword -> keyword.ignoreAbove(256)))))
                            .properties("description", property -> property.text(type -> type))
                            .properties("price", property -> property.scaledFloat(type -> type.scalingFactor(100.0)))
                            .properties("currency", property -> property.keyword(type -> type.ignoreAbove(16)))
                            .properties("billingCycle", property -> property.keyword(type -> type.ignoreAbove(32)))
                            .properties("billingInterval", property -> property.integer(type -> type))
                            .properties("nextBillingDate", property -> property.date(type -> type))
                            .properties("expireDate", property -> property.date(type -> type))
                            .properties("remindDaysBefore", property -> property.integer(type -> type))
                            .properties("autoRenew", property -> property.boolean_(type -> type))
                            .properties("status", property -> property.keyword(type -> type.ignoreAbove(32)))
                            .properties("website", property -> property.keyword(type -> type.ignoreAbove(512)))
                            .properties("remark", property -> property.text(type -> type))
                            .properties("createdAt", property -> property.date(type -> type))
                            .properties("updatedAt", property -> property.date(type -> type))
                            .properties("deleted", property -> property.boolean_(type -> type))));
            log.info("Created Elasticsearch index: {}", SearchIndexConstants.SUBSCRIPTION_INDEX);
        } catch (IOException | ElasticsearchException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建订阅搜索索引失败");
        }
    }

    @Override
    public void indexSubscription(SubscriptionEntity subscription) {
        ensureSubscriptionIndex();
        SubscriptionSearchDocument document = toDocument(subscription);
        try {
            elasticsearchClient.index(request -> request
                    .index(SearchIndexConstants.SUBSCRIPTION_INDEX)
                    .id(String.valueOf(document.id()))
                    .document(document));
        } catch (IOException | ElasticsearchException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "同步订阅搜索索引失败");
        }
    }

    @Override
    public void deleteSubscription(Long subscriptionId) {
        ensureSubscriptionIndex();
        try {
            elasticsearchClient.delete(request -> request
                    .index(SearchIndexConstants.SUBSCRIPTION_INDEX)
                    .id(String.valueOf(subscriptionId)));
        } catch (IOException | ElasticsearchException exception) {
            log.warn("Delete subscription search document failed: subscriptionId={}", subscriptionId, exception);
        }
    }

    @Override
    public SubscriptionPageVO searchCurrentUserSubscriptions(long page, long size, String keyword) {
        ensureSubscriptionIndex();
        Long userId = UserContext.getUserId();
        long currentPage = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int from = Math.toIntExact((currentPage - 1) * pageSize);
        try {
            SearchResponse<SubscriptionSearchDocument> response = elasticsearchClient.search(request -> request
                            .index(SearchIndexConstants.SUBSCRIPTION_INDEX)
                            .from(from)
                            .size(Math.toIntExact(pageSize))
                            .query(query -> query.bool(bool -> {
                                bool.filter(filter -> filter.term(term -> term.field("userId").value(userId)));
                                bool.filter(filter -> filter.term(term -> term.field("deleted").value(false)));
                                if (StringUtils.hasText(keyword)) {
                                    bool.must(must -> must.multiMatch(multiMatch -> multiMatch
                                            .query(keyword.trim())
                                            .fields("name^4", "provider^3", "categoryName^2", "description", "remark")));
                                } else {
                                    bool.must(must -> must.matchAll(matchAll -> matchAll));
                                }
                                return bool;
                            }))
                            .sort(sort -> sort.field(field -> field
                                    .field("nextBillingDate")
                                    .order(SortOrder.Asc)
                                    .missing("_last")))
                            .sort(sort -> sort.field(field -> field
                                    .field("createdAt")
                                    .order(SortOrder.Desc))),
                    SubscriptionSearchDocument.class);

            long total = response.hits().total() == null ? 0 : response.hits().total().value();
            long pages = total == 0 ? 0 : (total + pageSize - 1) / pageSize;
            List<SubscriptionVO> records = response.hits().hits().stream()
                    .map(Hit::source)
                    .map(this::toVO)
                    .toList();
            return new SubscriptionPageVO(currentPage, pageSize, total, pages, records);
        } catch (IOException | ElasticsearchException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "搜索订阅失败");
        }
    }

    @Override
    public long rebuildCurrentUserSubscriptions() {
        ensureSubscriptionIndex();
        Long userId = UserContext.getUserId();
        List<SubscriptionEntity> subscriptions = subscriptionMapper.selectList(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getUserId, userId));
        subscriptions.forEach(this::indexSubscription);
        return subscriptions.size();
    }

    @Override
    public SubscriptionSearchDocument toDocument(SubscriptionEntity subscription) {
        CategoryEntity category = null;
        if (subscription.getCategoryId() != null) {
            try {
                category = categoryService.getOwnedCategoryOrThrow(subscription.getUserId(), subscription.getCategoryId());
            } catch (BusinessException exception) {
                log.warn("Subscription category missing while building search document: subscriptionId={}, categoryId={}",
                        subscription.getId(), subscription.getCategoryId());
            }
        }
        return new SubscriptionSearchDocument(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getCategoryId(),
                category == null ? null : category.getName(),
                subscription.getName(),
                subscription.getProvider(),
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
                subscription.getUpdatedAt(),
                false
        );
    }

    private SubscriptionVO toVO(SubscriptionSearchDocument document) {
        return new SubscriptionVO(
                document.id(),
                document.name(),
                document.provider(),
                document.categoryId(),
                document.categoryName(),
                document.description(),
                document.price(),
                document.currency(),
                document.billingCycle(),
                document.billingInterval(),
                document.nextBillingDate(),
                document.expireDate(),
                document.remindDaysBefore(),
                document.autoRenew(),
                document.status(),
                document.website(),
                document.remark(),
                document.createdAt(),
                document.updatedAt()
        );
    }
}
