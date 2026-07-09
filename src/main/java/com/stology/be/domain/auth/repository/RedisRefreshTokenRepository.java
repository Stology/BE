package com.stology.be.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String email, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                "refresh:" + email,
                refreshToken,
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public Optional<String> findBySocialUid(String uid) {
        return Optional.ofNullable(redisTemplate.opsForValue().get("refresh:" + uid));
    }

    @Override
    public void delete(String email) {
        redisTemplate.delete("refresh:" + email);
    }
}
