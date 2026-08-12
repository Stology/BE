package com.stology.be.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisBlacklistTokenRepository implements BlacklistTokenRepository {

    private static final String KEY_PREFIX = "blacklist:access:";
    private static final String BLACKLISTED = "blacklisted";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String accessToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + accessToken,
                BLACKLISTED,
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public boolean exists(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + accessToken));
    }
}
