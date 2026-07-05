package com.stology.be.global.security.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthMemberTest {

    @Test
    void getName_returnsMemberName_whenMemberPresent() {
        Member member = Member.builder().name("Kakao User").socialType(SocialType.KAKAO)
                .socialUid("uid").email("test@example.com").build();
        OAuthMember oAuthMember = new OAuthMember(member, Map.of("id", 1L));

        assertThat(oAuthMember.getName()).isEqualTo("Kakao User");
    }

    @Test
    void getName_returnsEmptyString_whenMemberNull() {
        OAuthMember oAuthMember = new OAuthMember(null, Map.of());

        assertThat(oAuthMember.getName()).isEqualTo("");
    }

    @Test
    void getAttributes_returnsSuppliedMap() {
        Map<String, Object> attributes = Map.of("id", 1L, "email", "test@example.com");
        OAuthMember oAuthMember = new OAuthMember(null, attributes);

        assertThat(oAuthMember.getAttributes()).isEqualTo(attributes);
    }

    @Test
    void getAuthorities_isAlwaysEmpty() {
        OAuthMember oAuthMember = new OAuthMember(null, Map.of());

        assertThat(oAuthMember.getAuthorities()).isEmpty();
    }

    @Test
    void getMember_returnsConstructorValue() {
        Member member = Member.builder().name("tester").socialType(SocialType.KAKAO)
                .socialUid("uid").email("test@example.com").build();
        OAuthMember oAuthMember = new OAuthMember(member, Map.of());

        assertThat(oAuthMember.getMember()).isEqualTo(member);
    }

    @Test
    void implementsOAuth2User() {
        assertThat(new OAuthMember(null, Map.of())).isInstanceOf(OAuth2User.class);
    }
}