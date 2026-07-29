package com.stology.be.support;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.Study;

import java.time.LocalDate;

public final class TestFixtures {

    /**
     * 질문/답글 픽스처의 기본 작성자 ID. isMine·소유권 판별이 member_id 기준이므로 픽스처도 FK를 채운다.
     * memberName은 일부러 작성자 이름("member1")과 다른 값("writer")으로 둬서,
     * 판별이 이름 비교로 되돌아가면 테스트가 깨지게 한다.
     */
    public static final Long AUTHOR_ID = 1L;

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
        return question(id, study, member(AUTHOR_ID));
    }

    public static Question question(Long id, Study study, Member author) {
        return Question.builder()
                .id(id)
                .study(study)
                .member(author)
                .title("question title")
                .content("question content")
                .memberName("writer")
                .answerCount(0)
                .isAttached(false)
                .build();
    }

    public static Answer answer(Long id, Question question) {
        return answer(id, question, member(AUTHOR_ID));
    }

    public static Answer answer(Long id, Question question, Member author) {
        return Answer.builder()
                .id(id)
                .question(question)
                .member(author)
                .content("answer content")
                .memberName("writer")
                .build();
    }
}
