package com.stology.be.domain.auth.repository;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(String uid, String refreshToken, long ttlSeconds);
    Optional<String> findBySocialUid(String uid);
    void delete(String uid);
}
