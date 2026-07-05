package com.stology.be.domain.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResDTOTest {

    @Test
    void login_storesAccessAndRefreshTokens() {
        AuthResDTO.Login login = new AuthResDTO.Login("access-token", "refresh-token");

        assertThat(login.accessToken()).isEqualTo("access-token");
        assertThat(login.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_equalsAndHashCode_areBasedOnBothFields() {
        AuthResDTO.Login first = new AuthResDTO.Login("access", "refresh");
        AuthResDTO.Login second = new AuthResDTO.Login("access", "refresh");
        AuthResDTO.Login differentAccess = new AuthResDTO.Login("other-access", "refresh");
        AuthResDTO.Login differentRefresh = new AuthResDTO.Login("access", "other-refresh");

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(differentAccess);
        assertThat(first).isNotEqualTo(differentRefresh);
    }

    @Test
    void login_supportsNullTokens() {
        AuthResDTO.Login login = new AuthResDTO.Login(null, null);

        assertThat(login.accessToken()).isNull();
        assertThat(login.refreshToken()).isNull();
    }
}