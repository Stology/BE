package com.stology.be.domain.home.service;

import com.stology.be.domain.home.dto.res.AnswerDetailRes;
import com.stology.be.domain.home.dto.res.MaterialDetailRes;
import com.stology.be.domain.home.dto.res.QuestionDetailRes;
import com.stology.be.domain.inquiry.enums.InquiryStatus;
import com.stology.be.domain.inquiry.repository.InquiryReadRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.study.entity.*;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.global.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeSpecificInfoService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 10;

    private static final int ACTIVITY_PAGE_SIZE = 10;


    private final StudyMaterialRepository studyMaterialRepository;
    private final InquiryReadRepository inquiryReadRepository;
    private final InquiryReplyRepository inquiryReplyRepository;

    public MaterialDetailRes getMaterialDetail(
            Long memberId,
            Long cursor,
            Integer size
    ) {
        int pageSize = normalizeSize(size);

        Slice<StudyMaterial> materialSlice =
                findMaterials(
                        memberId,
                        cursor,
                        pageSize
                );

        return createMaterialDetailResponse(
                materialSlice
        );
    }

    //질문 상세조회

    public QuestionDetailRes getQuestionDetail(
            Long memberId,
            Long cursor
    ) {
        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        LocalDateTime endOfDay =
                startOfDay.plusDays(1);


        Slice<QuestionRead> questionSlice =
                inquiryReadRepository.findQuestionActivities(
                        memberId,
                        startOfDay,
                        endOfDay,
                        cursor,
                        PageRequest.of(
                                0,
                                ACTIVITY_PAGE_SIZE
                        )
                );

        return createQuestionDetailResponse(
                questionSlice
        );
    }

    //답변 상세 조회
    public AnswerDetailRes getAnswerDetail(
            Long memberId,
            Long cursor
    ) {
        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        LocalDateTime endOfDay =
                startOfDay.plusDays(1);

        Slice<Answer> answerSlice =
                inquiryReplyRepository.findAnswerActivities(
                        memberId,
                        startOfDay,
                        endOfDay,
                        cursor,
                        PageRequest.of(
                                0,
                                ACTIVITY_PAGE_SIZE
                        )
                );

        return createAnswerDetailResponse(
                answerSlice
        );
    }



    /*
    내부 매서드
     */
    private Slice<StudyMaterial> findMaterials(
            Long memberId,
            Long cursor,
            int pageSize
    ) {
        return studyMaterialRepository.findTodoMaterials(
                memberId,
                List.of(
                        DataState.NEEDREVIEW,
                        DataState.EXTRACTIONFAILED
                ),
                cursor,
                PageRequest.of(
                        0,
                        pageSize
                )
        );
    }


    private MaterialDetailRes createMaterialDetailResponse(
            Slice<StudyMaterial> materialSlice
    ) {
        List<StudyMaterial> materials =
                materialSlice.getContent();

        List<MaterialDetailRes.MaterialInfo> materialInfos =
                materials.stream()
                        .map(this::toMaterialInfo)
                        .toList();

        return MaterialDetailRes.builder()
                .materials(materialInfos)
                .pageInfo(
                        createPageInfo(
                                materials,
                                materialSlice.hasNext()
                        )
                )
                .build();
    }

    private MaterialDetailRes.MaterialInfo toMaterialInfo(
            StudyMaterial material
    ) {
        MemberStudy memberStudy =
                material.getMemberStudy();

        Study study =
                memberStudy.getStudy();

        return MaterialDetailRes.MaterialInfo.builder()
                .studyMaterialId(material.getId())
                .studyId(study.getId())
                .dataState(material.getDataState())
                .dataTitle(material.getDataTitle())
                .studyName(study.getName())
                .week(calculateWeek(study, material))
                .uploaderName(memberStudy.getMember().getName())
                .uploadedDate(material.getCreatedAt().toLocalDate())
                .build();
    }

    private long calculateWeek(
            Study study,
            StudyMaterial material
    ) {
        long elapsedDays = ChronoUnit.DAYS.between(
                study.getCreatedAt().toLocalDate(),
                material.getCreatedAt().toLocalDate()
        );

        return Math.max(
                1L,
                elapsedDays / 7L + 1L
        );
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }

    private PageInfo<Long> createPageInfo(
            List<StudyMaterial> materials,
            boolean hasNext
    ) {
        Long nextCursor =
                hasNext && !materials.isEmpty()
                        ? materials.get(materials.size() - 1).getId()
                        : null;

        return new PageInfo<>(
                nextCursor,
                materials.size(),
                hasNext
        );
    }




    private QuestionDetailRes createQuestionDetailResponse(
            Slice<QuestionRead> questionSlice
    ) {
        List<QuestionDetailRes.QuestionInfo> questions =
                questionSlice.getContent().stream()
                        .map(this::toQuestionInfo)
                        .toList();

        Long nextCursor =
                questionSlice.hasNext() && !questions.isEmpty()
                        ? questions.get(questions.size() - 1)
                        .getQuestionId()
                        : null;

        return QuestionDetailRes.builder()
                .questions(questions)
                .pageInfo(
                        new PageInfo<>(
                                nextCursor,
                                questions.size(),
                                questionSlice.hasNext()
                        )
                )
                .build();
    }

    private QuestionDetailRes.QuestionInfo toQuestionInfo(
            QuestionRead questionRead
    ) {
        Question question =
                questionRead.getQuestion();

        Study study =
                question.getStudy();

        return QuestionDetailRes.QuestionInfo.builder()
                .checked(
                        questionRead.getInquiryStatus()
                                == InquiryStatus.CHECKED
                )
                .studyId(study.getId())
                .questionId(question.getId())
                .questionTitle(question.getTitle())
                .studyName(study.getName())
                .writerName(question.getMemberName())
                .createdAt(question.getCreatedAt())
                .build();
    }

    private AnswerDetailRes createAnswerDetailResponse(
            Slice<Answer> answerSlice
    ) {
        List<AnswerDetailRes.AnswerInfo> answers =
                answerSlice.getContent().stream()
                        .map(this::toAnswerInfo)
                        .toList();

        Long nextCursor =
                answerSlice.hasNext() && !answers.isEmpty()
                        ? answers.get(answers.size() - 1)
                        .getAnswerId()
                        : null;

        return AnswerDetailRes.builder()
                .answers(answers)
                .pageInfo(
                        new PageInfo<>(
                                nextCursor,
                                answers.size(),
                                answerSlice.hasNext()
                        )
                )
                .build();
    }

    private AnswerDetailRes.AnswerInfo toAnswerInfo(
            Answer answer
    ) {
        Question question =
                answer.getQuestion();

        Study study =
                question.getStudy();

        return AnswerDetailRes.AnswerInfo.builder()
                .checked(answer.getReadAtByAsker() != null)
                .studyId(study.getId())
                .questionId(question.getId())
                .answerId(answer.getId())
                .questionTitle(question.getTitle())
                .studyName(study.getName())
                .writerName(answer.getMemberName())
                .createdAt(answer.getCreatedAt())
                .build();
    }





}