package com.stology.be.global.security.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class AuthMemberTest {

    @Test
    void getAuthorities_isAlwaysEmpty() {
        Member member = Member.builder().name("tester").socialType(SocialType.KAKAO)
                .socialUid("uid").email("test@example.com").build();
        AuthMember authMember = new AuthMember(member);

        assertThat(authMember.getAuthorities()).isEmpty();
    }

    @Test
    void getPassword_isEmptyString() {
        AuthMember authMember = new AuthMember(null);

        assertThat(authMember.getPassword()).isEqualTo("");
    }

    @Test
    void getUsername_isEmptyString() {
        AuthMember authMember = new AuthMember(null);

        assertThat(authMember.getUsername()).isEqualTo("");
    }

    @Test
    void getMember_returnsConstructorValue() {
        Member member = Member.builder().name("tester").socialType(SocialType.KAKAO)
                .socialUid("uid").email("test@example.com").build();
        AuthMember authMember = new AuthMember(member);

        assertThat(authMember.getMember()).isEqualTo(member);
    }

    @Test
    void allowsNullMember() {
        AuthMember authMember = new AuthMember(null);

        assertThat(authMember.getMember()).isNull();
    }

    @Test
    void implementsUserDetails() {
        assertThat(new AuthMember(null)).isInstanceOf(UserDetails.class);
    }
}