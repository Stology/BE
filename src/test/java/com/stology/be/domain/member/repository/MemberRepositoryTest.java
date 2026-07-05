package com.stology.be.domain.member.repository;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:member-repository-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findBySocialTypeAndSocialUid_returnsMember_whenMatchExists() {
        Member member = Member.builder()
                .name("Tester")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-100")
                .email("tester@example.com")
                .build();
        memberRepository.save(member);

        Optional<Member> found = memberRepository.findBySocialTypeAndSocialUid(SocialType.KAKAO, "uid-100");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Tester");
        assertThat(found.get().getEmail()).isEqualTo("tester@example.com");
        assertThat(found.get().getSocialType()).isEqualTo(SocialType.KAKAO);
    }

    @Test
    void findBySocialTypeAndSocialUid_returnsEmpty_whenSocialUidDoesNotMatch() {
        Member member = Member.builder()
                .name("Tester")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-100")
                .email("tester@example.com")
                .build();
        memberRepository.save(member);

        Optional<Member> found = memberRepository.findBySocialTypeAndSocialUid(SocialType.KAKAO, "does-not-exist");

        assertThat(found).isEmpty();
    }

    @Test
    void findBySocialTypeAndSocialUid_returnsEmpty_whenRepositoryIsEmpty() {
        Optional<Member> found = memberRepository.findBySocialTypeAndSocialUid(SocialType.KAKAO, "uid-100");

        assertThat(found).isEmpty();
    }

    @Test
    void save_generatesIdForNewMember() {
        Member member = Member.builder()
                .name("New User")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-200")
                .email("new@example.com")
                .build();

        Member saved = memberRepository.save(member);

        assertThat(saved.getId()).isNotNull();
    }
}