package com.stology.be.global.security.util;

import com.stology.be.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET_KEY = "test-secret-key-test-secret-key-test-secret-key";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_KEY, 60_000L, 120_000L);
    }

    @Test
    void createAccessToken_producesTokenThatIsValid() {
        AuthMember authMember = new AuthMember(null);

        String token = jwtUtil.createAccessToken(authMember);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void createRefreshToken_producesTokenThatIsValid() {
        AuthMember authMember = new AuthMember(null);

        String token = jwtUtil.createRefreshToken(authMember);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void getEmail_returnsSubjectClaimOfIssuedToken() {
        // AuthMember#getUsername() always returns "" (see AuthMember implementation),
        // so the JWT subject/email claim is currently always blank. This test documents
        // that current behaviour end-to-end via the real token creation/parsing path.
        AuthMember authMember = new AuthMember(null);
        String token = jwtUtil.createAccessToken(authMember);

        assertThat(jwtUtil.getEmail(token)).isEqualTo("");
    }

    @Test
    void getEmail_returnsNullForMalformedToken() {
        assertThat(jwtUtil.getEmail("not-a-valid-jwt")).isNull();
    }

    @Test
    void isValid_returnsFalseForMalformedToken() {
        assertThat(jwtUtil.isValid("not-a-valid-jwt")).isFalse();
    }

    @Test
    void isValid_throwsIllegalArgumentExceptionForNullToken() {
        // jjwt's parser rejects null/blank input with IllegalArgumentException before any
        // JwtException can be thrown, so isValid does not treat null as "invalid" gracefully.
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jwtUtil.isValid(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isValid_returnsFalseForTamperedToken() {
        AuthMember authMember = new AuthMember(null);
        String token = jwtUtil.createAccessToken(authMember);
        char lastChar = token.charAt(token.length() - 1);
        String tampered = token.substring(0, token.length() - 1) + (lastChar == 'a' ? 'b' : 'a');

        assertThat(jwtUtil.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_returnsFalseForTokenSignedWithDifferentSecretKey() {
        JwtUtil otherJwtUtil = new JwtUtil(
                "different-secret-key-different-secret-key-32bytes", 60_000L, 120_000L);
        String token = otherJwtUtil.createAccessToken(new AuthMember(null));

        assertThat(jwtUtil.isValid(token)).isFalse();
    }

    @Test
    void isValid_returnsFalseForExpiredToken() throws InterruptedException {
        JwtUtil shortLivedJwtUtil = new JwtUtil(SECRET_KEY, 1L, 1L);
        String token = shortLivedJwtUtil.createAccessToken(new AuthMember(null));

        Thread.sleep(1500);

        assertThat(shortLivedJwtUtil.isValid(token)).isFalse();
    }
}