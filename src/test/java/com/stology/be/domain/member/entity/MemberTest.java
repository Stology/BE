package com.stology.be.domain.member.entity;

import com.stology.be.domain.member.enums.SocialType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void builder_createsInstanceWithAllFieldsSet() {
        Member member = Member.builder()
                .id(1L)
                .name("tester")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-1")
                .email("test@test.com")
                .build();

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getName()).isEqualTo("tester");
        assertThat(member.getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(member.getSocialUid()).isEqualTo("uid-1");
        assertThat(member.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void noArgsConstructor_createsInstanceWithNullFields() {
        Member member = new Member();

        assertThat(member.getId()).isNull();
        assertThat(member.getName()).isNull();
        assertThat(member.getSocialType()).isNull();
        assertThat(member.getSocialUid()).isNull();
        assertThat(member.getEmail()).isNull();
    }

    @Test
    void validation_passesForFullyValidMember() {
        Member member = validMemberBuilder().build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_failsWhenNameIsBlank() {
        Member member = validMemberBuilder().name("").build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void validation_failsWhenSocialTypeIsNull() {
        Member member = validMemberBuilder().socialType(null).build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("socialType");
    }

    @Test
    void validation_failsWhenSocialUidIsBlank() {
        Member member = validMemberBuilder().socialUid(" ").build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("socialUid");
    }

    @Test
    void validation_failsWhenEmailIsNotWellFormed() {
        Member member = validMemberBuilder().email("not-an-email").build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("email");
    }

    private Member.MemberBuilder validMemberBuilder() {
        return Member.builder()
                .name("tester")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-1")
                .email("test@test.com");
    }
}