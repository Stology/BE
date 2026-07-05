package com.stology.be.domain.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthReqDTOTest {

    @Test
    void reissue_holdsRefreshTokenValue() {
        AuthReqDTO.Reissue reissue = new AuthReqDTO.Reissue("some-refresh-token");

        assertThat(reissue.refreshToken()).isEqualTo("some-refresh-token");
    }

    @Test
    void reissue_allowsNullRefreshToken() {
        AuthReqDTO.Reissue reissue = new AuthReqDTO.Reissue(null);

        assertThat(reissue.refreshToken()).isNull();
    }

    @Test
    void reissue_equalsAndHashCode_areValueBased() {
        AuthReqDTO.Reissue a = new AuthReqDTO.Reissue("token");
        AuthReqDTO.Reissue b = new AuthReqDTO.Reissue("token");
        AuthReqDTO.Reissue c = new AuthReqDTO.Reissue("other");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }
}