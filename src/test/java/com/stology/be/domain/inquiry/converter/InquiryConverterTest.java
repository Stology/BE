package com.stology.be.domain.inquiry.converter;

import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InquiryConverterTest {

    @Test
    void toQuestion_shouldMapRequestAndEntities() {
        InquiryReqDTO.WriteQuestion request = new InquiryReqDTO.WriteQuestion("제목", "본문", null);
        Study study = TestFixtures.study(4L);
        Member member = TestFixtures.member(3L);

        Question question = InquiryConverter.toQuestion(request, study, member);

        assertNotNull(question);
        assertEquals(study, question.getStudy());
        assertEquals("제목", question.getTitle());
        assertEquals("본문", question.getContent());
        assertEquals(member.getName(), question.getMemberName());
        assertEquals(0, question.getAnswerCount());
        assertFalse(question.getIsAttached());
    }

    @Test
    void toQuestionSummary_shouldMarkMyQuestion() {
        Question question = TestFixtures.question(1L, TestFixtures.study(5L));
        question.updateAttached(true);

        InquiryResDTO.QuestionSummary summary = InquiryConverter.toQuestionSummary(question, "writer");

        assertNotNull(summary);
        assertEquals(question.getId(), summary.questionId());
        assertEquals(question.getTitle(), summary.title());
        assertEquals(question.getMemberName(), summary.authorName());
        assertTrue(summary.isMine());
        assertTrue(summary.hasImage());
    }

    @Test
    void toQuestionList_shouldBuildPaginationResponse() {
        Question question = TestFixtures.question(10L, TestFixtures.study(6L));
        Page<Question> page = new PageImpl<>(List.of(question), org.springframework.data.domain.Pageable.unpaged(), 1);

        InquiryResDTO.QuestionList response = InquiryConverter.toQuestionList(page, true, "writer");

        assertNotNull(response);
        assertEquals(1, response.questionList().size());
        assertEquals(1, response.listSize());
        assertEquals(1, response.totalPage());
        assertEquals(1L, response.totalElements());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertTrue(response.studyEnded());
    }

    @Test
    void toAnswerDetail_shouldIncludeImagesAndOwnership() {
        Answer answer = TestFixtures.answer(99L, TestFixtures.question(100L, TestFixtures.study(7L)));
        answer.updateContent("답변입니다");

        InquiryResDTO.AnswerDetail detail = InquiryConverter.toAnswerDetail(
                answer,
                List.of(
                        new InquiryResDTO.ImageInfo(1L, "img1.png", "display1.png"),
                        new InquiryResDTO.ImageInfo(2L, "img2.png", "display2.png")
                ),
                "writer"
        );

        assertNotNull(detail);
        assertEquals(answer.getId(), detail.answerId());
        assertEquals("답변입니다", detail.content());
        assertEquals(2, detail.images().size());
        assertTrue(detail.isMine());
    }

    @Test
    void toQuestionDetail_shouldIncludeAnswersAndStudyStatus() {
        Question question = TestFixtures.question(200L, TestFixtures.study(8L));
        Answer answer = TestFixtures.answer(201L, question);
        answer.updateContent("답변");

        InquiryResDTO.QuestionDetail detail = InquiryConverter.toQuestionDetail(
                question,
                List.of(new InquiryResDTO.ImageInfo(1L, "img.png", "display.png")),
                List.of(InquiryConverter.toAnswerDetail(answer, List.of(), "writer")),
                false,
                "writer"
        );

        assertNotNull(detail);
        assertEquals(question.getId(), detail.questionId());
        assertEquals("question title", detail.title());
        assertEquals(1, detail.answerList().size());
        assertFalse(detail.studyEnded());
        assertTrue(detail.isMine());
    }
}
