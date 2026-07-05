package com.stology.be.domain.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthReqDTOTest {

    @Test
    void reissue_storesRefreshTokenValue() {
        AuthReqDTO.Reissue reissue = new AuthReqDTO.Reissue("token-123");

        assertThat(reissue.refreshToken()).isEqualTo("token-123");
    }

    @Test
    void reissue_supportsNullRefreshToken() {
        AuthReqDTO.Reissue reissue = new AuthReqDTO.Reissue(null);

        assertThat(reissue.refreshToken()).isNull();
    }

    @Test
    void reissue_equalsAndHashCode_areBasedOnRefreshToken() {
        AuthReqDTO.Reissue first = new AuthReqDTO.Reissue("same-token");
        AuthReqDTO.Reissue second = new AuthReqDTO.Reissue("same-token");
        AuthReqDTO.Reissue different = new AuthReqDTO.Reissue("other-token");

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(different);
    }

    @Test
    void reissue_toString_containsFieldValue() {
        AuthReqDTO.Reissue reissue = new AuthReqDTO.Reissue("token-123");

        assertThat(reissue.toString()).contains("token-123");
    }
}