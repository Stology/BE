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
    void builder_setsAllFields() {
        Member member = Member.builder()
                .id(1L)
                .name("Test User")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-123")
                .email("test@example.com")
                .build();

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getName()).isEqualTo("Test User");
        assertThat(member.getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(member.getSocialUid()).isEqualTo("uid-123");
        assertThat(member.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void noArgsConstructor_createsEmptyInstance() {
        Member member = new Member();

        assertThat(member.getId()).isNull();
        assertThat(member.getName()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        Member member = new Member(2L, "Another User", SocialType.KAKAO, "uid-456", "another@example.com");

        assertThat(member.getId()).isEqualTo(2L);
        assertThat(member.getName()).isEqualTo("Another User");
        assertThat(member.getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(member.getSocialUid()).isEqualTo("uid-456");
        assertThat(member.getEmail()).isEqualTo("another@example.com");
    }

    @Test
    void validation_passesForValidMember() {
        Member member = validMemberBuilder().build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_failsWhenNameIsBlank() {
        Member member = validMemberBuilder().name("").build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void validation_failsWhenSocialTypeIsNull() {
        Member member = validMemberBuilder().socialType(null).build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("socialType"));
    }

    @Test
    void validation_failsWhenSocialUidIsBlank() {
        Member member = validMemberBuilder().socialUid(" ").build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("socialUid"));
    }

    @Test
    void validation_failsWhenEmailIsInvalid() {
        Member member = validMemberBuilder().email("not-an-email").build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void validation_failsWhenEmailIsBlank() {
        Member member = validMemberBuilder().email("").build();

        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    private Member.MemberBuilder validMemberBuilder() {
        return Member.builder()
                .name("Test User")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-123")
                .email("test@example.com");
    }
}