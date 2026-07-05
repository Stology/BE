package com.stology.be.domain.member.repository;

import com.stology.be.domain.member.enums.SocialType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRepositoryTest {

    @Test
    void extendsJpaRepository() {
        assertThat(JpaRepository.class).isAssignableFrom(MemberRepository.class);
    }

    @Test
    void declaresFindBySocialTypeAndSocialUidWithExpectedSignature() throws NoSuchMethodException {
        Method method = MemberRepository.class.getMethod(
                "findBySocialTypeAndSocialUid", SocialType.class, String.class);

        assertThat(method.getReturnType()).isEqualTo(Optional.class);
    }
}