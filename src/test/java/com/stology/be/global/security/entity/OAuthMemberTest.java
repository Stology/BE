package com.stology.be.global.security.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthMemberTest {

    @Test
    void getName_returnsMemberNameWhenMemberPresent() {
        Member member = Member.builder()
                .name("tester")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-1")
                .email("test@test.com")
                .build();
        Map<String, Object> attributes = Map.of("id", 1L);

        OAuthMember oAuthMember = new OAuthMember(member, attributes);

        assertThat(oAuthMember.getName()).isEqualTo("tester");
        assertThat(oAuthMember.getMember()).isSameAs(member);
    }

    @Test
    void getName_returnsEmptyStringWhenMemberIsNull() {
        OAuthMember oAuthMember = new OAuthMember(null, Map.of());

        assertThat(oAuthMember.getName()).isEqualTo("");
    }

    @Test
    void getAttributes_returnsProvidedAttributesMap() {
        Map<String, Object> attributes = Map.of("id", 1L, "email", "test@test.com");

        OAuthMember oAuthMember = new OAuthMember(null, attributes);

        assertThat(oAuthMember.getAttributes()).isEqualTo(attributes);
    }

    @Test
    void getAuthorities_returnsEmptyCollection() {
        OAuthMember oAuthMember = new OAuthMember(null, Map.of());

        assertThat(oAuthMember.getAuthorities()).isEmpty();
    }
}