package com.stology.be.domain.home.service;

import com.stology.be.domain.home.dto.res.MyTodoRes;
import com.stology.be.domain.inquiry.enums.InquiryStatus;
import com.stology.be.domain.inquiry.repository.InquiryReadRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.upload.enums.DataState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeInfoService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final InquiryReadRepository inquiryReadRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final MemberStudyRepository memberStudyRepository;

    public MyTodoRes getMyTodos(Long memberId) {
        MyTodoRes.MyTodoResBuilder response =
                MyTodoRes.builder();

        //자료 조회
        setMaterialTodos(
                response,
                memberId
        );
        //질문 조회
        setQuestionTodos(
                response,
                memberId
        );
        //리포트 조회
        setReportTodos(
                response,
                memberId
        );

        return response.build();
    }







    /*
    내부 함수
     */
    private void setMaterialTodos(
            MyTodoRes.MyTodoResBuilder response,
            Long memberId
    ) {
        long reviewCount =
                studyMaterialRepository
                        .countByMemberStudyMemberIdAndDataState(
                                memberId,
                                DataState.NEEDREVIEW
                        );

        long reUploadCount =
                studyMaterialRepository
                        .countByMemberStudyMemberIdAndDataState(
                                memberId,
                                DataState.EXTRACTIONFAILED
                        );

        response
                .reviewCount(reviewCount)
                .reUploadCount(reUploadCount);
    }

    private void setQuestionTodos(
            MyTodoRes.MyTodoResBuilder response,
            Long memberId
    ) {
        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        LocalDateTime endOfDay =
                startOfDay.plusDays(1);

        List<Long> questionIds =
                inquiryReadRepository.findTodoQuestionIds(
                        memberId,
                        InquiryStatus.UNCHECKED,
                        InquiryStatus.CHECKED,
                        startOfDay,
                        endOfDay
                );

        long answerCount =
                inquiryReplyRepository
                        .countTodoAnswersForMember(
                                memberId,
                                startOfDay,
                                endOfDay
                        );


        response
                .questionCount(questionIds.size())
                .answerCount(answerCount);
    }

    private void setReportTodos(
            MyTodoRes.MyTodoResBuilder response,
            Long memberId
    ) {
        long studyCount =
                memberStudyRepository.countByMemberId(memberId);

        response.studyCount(studyCount);
    }




}