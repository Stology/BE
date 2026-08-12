package com.stology.be.domain.auth.repository;

public interface BlacklistTokenRepository {
    void save(String accessToken, long ttlSeconds);
    boolean exists(String accessToken);
}
