package com.relog.relog.settlement.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relog.relog.settlement.dto.MonthlySettlementResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SettlementCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String MONTHLY_KEY_PREFIX = "settlement:monthly:";
    private static final String QUARTERLY_KEY_PREFIX = "settlement:quarterly:";
    private static final long TTL_DAYS = 30;

    public void saveMonthly(Long memberId, int year, int month, MonthlySettlementResponse response) {
        String key = MONTHLY_KEY_PREFIX + memberId + ":" + year + "-" + month;
        saveToRedis(key, response);
    }

    public Optional<MonthlySettlementResponse> findMonthly(Long memberId, int year, int month) {
        String key = MONTHLY_KEY_PREFIX + memberId + ":" + year + "-" + month;
        return findFromRedis(key, MonthlySettlementResponse.class);
    }

    public void saveQuarterly(Long memberId, int year, int quarter, QuarterlySettlementResponse response) {
        String key = QUARTERLY_KEY_PREFIX + memberId + ":" + year + "-" + quarter;
        saveToRedis(key, response);
    }

    public Optional<QuarterlySettlementResponse> findQuarterly(Long memberId, int year, int quarter) {
        String key = QUARTERLY_KEY_PREFIX + memberId + ":" + year + "-" + quarter;
        return findFromRedis(key, QuarterlySettlementResponse.class);
    }

    private void saveToRedis(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, TTL_DAYS, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("[SettlementCache] Failed to serialize: {}", e.getMessage());
        }
    }

    private <T> Optional<T> findFromRedis(String key, Class<T> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            log.error("[SettlementCache] Failed to deserialize: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
