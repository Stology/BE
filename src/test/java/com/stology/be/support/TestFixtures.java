package com.stology.be.support;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.Study;

import java.time.LocalDate;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Member member(Long id) {
        return Member.builder()
                .id(id)
                .name("member" + id)
                .socialType(SocialType.KAKAO)
                .socialUid("uid-" + id)
                .email("member" + id + "@test.com")
                .build();
    }

    public static Template template(Long id) {
        return Template.builder()
                .id(id)
                .name("template" + id)
                .description("test template")
                .build();
    }

    public static Study study(Long id) {
        return Study.builder()
                .id(id)
                .name("study" + id)
                .description("study description")
                .leaderMemberId(1L)
                .startDate(LocalDate.of(2026, 7, 1))
                .build();
    }

    public static Question question(Long id, Study study) {
        return Question.builder()
                .id(id)
                .study(study)
                .title("question title")
                .content("question content")
                .memberName("writer")
                .answerCount(0)
                .isAttached(false)
                .build();
    }

    public static Answer answer(Long id, Question question) {
        return Answer.builder()
                .id(id)
                .question(question)
                .content("answer content")
                .memberName("writer")
                .build();
    }
}
