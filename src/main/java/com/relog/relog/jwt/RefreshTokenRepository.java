package com.relog.relog.jwt;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "refresh_token:";

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public void save(Long memberId, String refreshToken) {
        String key = KEY_PREFIX + memberId;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }

    public String findByMemberId(Long memberId) {
        String key = KEY_PREFIX + memberId;
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(Long memberId) {
        String key = KEY_PREFIX + memberId;
        redisTemplate.delete(key);
    }

    public boolean exists(Long memberId) {
        String key = KEY_PREFIX + memberId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
