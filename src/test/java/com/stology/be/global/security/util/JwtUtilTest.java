package com.stology.be.global.security.util;

import com.stology.be.global.security.entity.AuthMember;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key";
    private static final long ACCESS_EXPIRATION_MS = 600_000L;
    private static final long REFRESH_EXPIRATION_MS = 1_200_000L;

    private JwtUtil jwtUtil;
    private AuthMember authMember;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
        authMember = new AuthMember(null);
    }

    @Test
    void createAccessToken_producesValidToken() {
        String token = jwtUtil.createAccessToken(authMember);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void createRefreshToken_producesValidToken() {
        String token = jwtUtil.createRefreshToken(authMember);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void createAccessToken_includesExpectedClaims() {
        String token = jwtUtil.createAccessToken(authMember);

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);

        assertThat(claims.getPayload().getSubject()).isEqualTo("");
        assertThat(claims.getPayload().get("email")).isEqualTo("");
        assertThat(claims.getPayload().get("role")).isEqualTo("");
        assertThat(claims.getPayload().getExpiration()).isAfter(claims.getPayload().getIssuedAt());
    }

    @Test
    void getEmail_returnsSubjectClaim_forValidToken() {
        String token = jwtUtil.createAccessToken(authMember);

        assertThat(jwtUtil.getEmail(token)).isEqualTo("");
    }

    @Test
    void getEmail_returnsNull_forMalformedToken() {
        assertThat(jwtUtil.getEmail("not-a-valid-jwt")).isNull();
    }

    @Test
    void isValid_returnsFalse_forMalformedToken() {
        assertThat(jwtUtil.isValid("not-a-valid-jwt")).isFalse();
    }

    @Test
    void isValid_returnsFalse_forNullToken() {
        assertThat(jwtUtil.isValid(null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forExpiredToken() {
        JwtUtil expiringJwtUtil = new JwtUtil(SECRET, -1_000L, -1_000L);
        String expiredToken = expiringJwtUtil.createAccessToken(authMember);

        assertThat(jwtUtil.isValid(expiredToken)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forTokenSignedWithDifferentSecret() {
        JwtUtil otherJwtUtil = new JwtUtil("different-secret-key-different-secret-key!!", ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
        String tokenFromOtherIssuer = otherJwtUtil.createAccessToken(authMember);

        assertThat(jwtUtil.isValid(tokenFromOtherIssuer)).isFalse();
    }
}