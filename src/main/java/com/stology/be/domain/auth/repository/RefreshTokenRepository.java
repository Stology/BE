package com.stology.be.domain.auth.repository;

public interface RefreshTokenRepository {
    void save(String email, String refreshToken, long ttlSeconds);
    String findByMemberId(String email);
    void delete(String email);
}
