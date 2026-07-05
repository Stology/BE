package com.stology.be.domain.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResDTOTest {

    @Test
    void login_holdsAccessAndRefreshTokens() {
        AuthResDTO.Login login = new AuthResDTO.Login("access-token", "refresh-token");

        assertThat(login.accessToken()).isEqualTo("access-token");
        assertThat(login.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_equalsAndHashCode_areValueBased() {
        AuthResDTO.Login a = new AuthResDTO.Login("access", "refresh");
        AuthResDTO.Login b = new AuthResDTO.Login("access", "refresh");
        AuthResDTO.Login c = new AuthResDTO.Login("different", "refresh");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }
}