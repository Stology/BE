package com.stology.be.domain.inquiry.converter;

import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.AnswerImage;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.QuestionImage;
import com.stology.be.domain.study.entity.Study;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class InquiryConverter {

    public static Question toQuestion(InquiryReqDTO.WriteQuestion request, Study study, Member member) {
        return Question.builder()
                .study(study)
                .title(request.title())
                .content(request.content())
                .memberName(member.getName())
                .answerCount(0)
                .isAttached(false)
                .build();
    }

    public static QuestionImage toQuestionImage(String imageUrl, Question question) {
        return QuestionImage.builder()
                .question(question)
                .imageUrl(imageUrl)
                .build();
    }

    public static Answer toAnswer(InquiryReqDTO.WriteAnswer request, Question question, Member member) {
        return Answer.builder()
                .question(question)
                .content(request.content())
                .memberName(member.getName())
                .build();
    }

    public static AnswerImage toAnswerImage(String imageUrl, Answer answer) {
        return AnswerImage.builder()
                .answer(answer)
                .imageUrl(imageUrl)
                .build();
    }

    public static InquiryResDTO.QuestionSummary toQuestionSummary(Question question, String currentMemberName) {
        return new InquiryResDTO.QuestionSummary(
                question.getId(),
                question.getTitle(),
                question.getMemberName(),
                question.getCreatedAt(),
                question.getAnswerCount(),
                question.getIsAttached(),
                question.getMemberName().equals(currentMemberName)
        );
    }

    public static InquiryResDTO.QuestionList toQuestionList(Page<Question> questionPage, boolean studyEnded, String currentMemberName) {
        List<InquiryResDTO.QuestionSummary> questionList = questionPage.stream()
                .map(question -> toQuestionSummary(question, currentMemberName))
                .collect(Collectors.toList());

        return new InquiryResDTO.QuestionList(
                questionList,
                questionList.size(),
                questionPage.getTotalPages(),
                questionPage.getTotalElements(),
                questionPage.isFirst(),
                questionPage.isLast(),
                studyEnded
        );
    }

    public static InquiryResDTO.AnswerDetail toAnswerDetail(Answer answer, List<String> imageUrls, String currentMemberName) {
        return new InquiryResDTO.AnswerDetail(
                answer.getId(),
                answer.getMemberName(),
                answer.getCreatedAt(),
                answer.getContent(),
                imageUrls,
                answer.getMemberName().equals(currentMemberName)
        );
    }

    public static InquiryResDTO.QuestionDetail toQuestionDetail(
            Question question,
            List<String> imageUrls,
            List<InquiryResDTO.AnswerDetail> answerList,
            boolean studyEnded,
            String currentMemberName
    ) {
        return new InquiryResDTO.QuestionDetail(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getMemberName(),
                question.getCreatedAt(),
                imageUrls,
                answerList,
                studyEnded,
                question.getMemberName().equals(currentMemberName)
        );
    }
}
