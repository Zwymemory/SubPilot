package com.subpilot.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subpilot.module.subscription.vo.SubscriptionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Duration SUBSCRIPTION_DETAIL_TTL = Duration.ofMinutes(30);
    private static final Duration EMPTY_VALUE_TTL = Duration.ofMinutes(2);
    private static final String EMPTY_VALUE = "__EMPTY__";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<SubscriptionVO> getSubscriptionDetail(Long userId, Long subscriptionId) {
        try {
            String value = stringRedisTemplate.opsForValue().get(subscriptionDetailKey(userId, subscriptionId));
            if (value == null) {
                return Optional.empty();
            }
            if (EMPTY_VALUE.equals(value)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(value, SubscriptionVO.class));
        } catch (Exception exception) {
            log.debug("Read subscription detail cache failed: userId={}, subscriptionId={}", userId, subscriptionId, exception);
            return Optional.empty();
        }
    }

    public void setSubscriptionDetail(Long userId, Long subscriptionId, SubscriptionVO subscription) {
        try {
            stringRedisTemplate.opsForValue().set(
                    subscriptionDetailKey(userId, subscriptionId),
                    objectMapper.writeValueAsString(subscription),
                    SUBSCRIPTION_DETAIL_TTL
            );
        } catch (JsonProcessingException exception) {
            log.debug("Serialize subscription detail cache failed: userId={}, subscriptionId={}", userId, subscriptionId, exception);
        } catch (Exception exception) {
            log.debug("Write subscription detail cache failed: userId={}, subscriptionId={}", userId, subscriptionId, exception);
        }
    }

    public void setEmptySubscriptionDetail(Long userId, Long subscriptionId) {
        try {
            stringRedisTemplate.opsForValue().set(subscriptionDetailKey(userId, subscriptionId), EMPTY_VALUE, EMPTY_VALUE_TTL);
        } catch (Exception exception) {
            log.debug("Write empty subscription detail cache failed: userId={}, subscriptionId={}", userId, subscriptionId, exception);
        }
    }

    public void evictSubscriptionDetail(Long userId, Long subscriptionId) {
        try {
            stringRedisTemplate.delete(subscriptionDetailKey(userId, subscriptionId));
        } catch (Exception exception) {
            log.debug("Evict subscription detail cache failed: userId={}, subscriptionId={}", userId, subscriptionId, exception);
        }
    }

    public void evictDashboard(Long userId) {
        try {
            stringRedisTemplate.delete("subpilot:dashboard:summary:" + userId);
            stringRedisTemplate.delete("subpilot:dashboard:monthly-trend:" + userId);
            stringRedisTemplate.delete("subpilot:dashboard:category-expense:" + userId);
        } catch (Exception exception) {
            log.debug("Evict dashboard cache failed: userId={}", userId, exception);
        }
    }

    public void evictCategoryList(Long userId) {
        try {
            stringRedisTemplate.delete("subpilot:category:list:" + userId);
        } catch (Exception exception) {
            log.debug("Evict category list cache failed: userId={}", userId, exception);
        }
    }

    private String subscriptionDetailKey(Long userId, Long subscriptionId) {
        return "subpilot:subscription:detail:%d:%d".formatted(userId, subscriptionId);
    }
}
