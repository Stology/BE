package com.stology.be.global.security.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthMemberTest {

    @Test
    void getMember_returnsMemberPassedToConstructor() {
        Member member = Member.builder()
                .id(1L)
                .name("tester")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-1")
                .email("test@test.com")
                .build();

        AuthMember authMember = new AuthMember(member);

        assertThat(authMember.getMember()).isSameAs(member);
    }

    @Test
    void getMember_supportsNullMember() {
        AuthMember authMember = new AuthMember(null);

        assertThat(authMember.getMember()).isNull();
    }

    @Test
    void getAuthorities_returnsEmptyCollection() {
        AuthMember authMember = new AuthMember(null);

        assertThat(authMember.getAuthorities()).isEmpty();
    }

    @Test
    void getPassword_returnsEmptyString() {
        AuthMember authMember = new AuthMember(null);

        assertThat(authMember.getPassword()).isEqualTo("");
    }

    @Test
    void getUsername_returnsEmptyString() {
        Member member = Member.builder().name("tester").socialType(SocialType.KAKAO)
                .socialUid("uid-1").email("test@test.com").build();

        AuthMember authMember = new AuthMember(member);

        assertThat(authMember.getUsername()).isEqualTo("");
    }
}