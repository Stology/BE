package com.stology.be.domain.home.service;

import com.stology.be.domain.home.converter.CursorConverter;
import com.stology.be.domain.home.dto.QuestionActivity;
import com.stology.be.domain.home.dto.res.MaterialDetailRes;
import com.stology.be.domain.home.dto.res.QuestionDetailRes;
import com.stology.be.domain.home.enums.QuestionActivityType;
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

    //조회 갯수
    private static final int QUESTION_PAGE_SIZE = 10;
    // 응답 갯수
    private static final int FETCH_SIZE =
            QUESTION_PAGE_SIZE + 1;


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

    public QuestionDetailRes getQuestionDetail(
            Long memberId,
            String cursor
    ) {
        CursorConverter.CursorValue cursorValue =
                CursorConverter.decode(cursor);

//이 범위는 오늘 읽은 질문과 답글을 자정 전까지 계속 보여주기 위해 사용
        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        LocalDateTime endOfDay =
                startOfDay.plusDays(1);

        //UNCHECKED 또는 CHECKED 이면서 오늘 읽은 질문
        List<QuestionActivity> questionActivities =
                findQuestionActivities(
                        memberId,
                        cursorValue,
                        startOfDay,
                        endOfDay
                );
        //내 질문의 새 답글 조회
        List<QuestionActivity> answerActivities =
                findAnswerActivities(
                        memberId,
                        cursorValue,
                        startOfDay,
                        endOfDay
                );
        // 질문, 답글 합치기. 작성 시간 최신순, 시간이 같으면 질문이 먼저
        List<QuestionActivity> mergedActivities =
                mergeActivities(
                        questionActivities,
                        answerActivities
                );

        return createQuestionDetailResponse(
                mergedActivities
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


    //질문 조회
    private List<QuestionActivity> findQuestionActivities(
            Long memberId,
            CursorConverter.CursorValue cursor,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    ) {
        Slice<QuestionRead> questionReads =
                inquiryReadRepository.findQuestionActivities(
                        memberId,
                        startOfDay,
                        endOfDay,
                        cursor.createdAt(),
                        cursor.typeRank(),
                        cursor.id(),
                        PageRequest.of(
                                0,
                                FETCH_SIZE
                        )
                );

        return questionReads.getContent().stream()
                .map(this::toQuestionActivity)
                .toList();
    }

    //답글 조회
    private List<QuestionActivity> findAnswerActivities(
            Long memberId,
            CursorConverter.CursorValue cursor,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    ) {
        Slice<Answer> answers =
                inquiryReplyRepository.findAnswerActivities(
                        memberId,
                        startOfDay,
                        endOfDay,
                        cursor.createdAt(),
                        cursor.typeRank(),
                        cursor.id(),
                        PageRequest.of(
                                0,
                                FETCH_SIZE
                        )
                );

        return answers.getContent().stream()
                .map(this::toAnswerActivity)
                .toList();
    }

    //질문과 답글 합치기.
    private List<QuestionActivity> mergeActivities(
            List<QuestionActivity> questions,
            List<QuestionActivity> answers
    ) {
        return Stream.concat(
                        questions.stream(),
                        answers.stream()
                )
                .sorted(
                        Comparator
                                .comparing(
                                        QuestionActivity::getCreatedAt,
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(
                                        QuestionActivity::getTypeRank,
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(
                                        QuestionActivity::getCursorId,
                                        Comparator.reverseOrder()
                                )
                )
                .limit(FETCH_SIZE)
                .toList();
    }
    private QuestionActivity toQuestionActivity(
            QuestionRead questionRead
    ) {
        Question question =
                questionRead.getQuestion();

        Study study =
                question.getStudy();

        return QuestionActivity.builder()
                .activityType(QuestionActivityType.QUESTION)
                .checked(
                        questionRead.getInquiryStatus()
                                == InquiryStatus.CHECKED
                )
                .studyId(study.getId())
                .questionId(question.getId())
                .answerId(null)
                .questionTitle(question.getTitle())
                .studyName(study.getName())
                .writerName(question.getMemberName())
                .createdAt(question.getCreatedAt())
                .build();
    }

    private QuestionActivity toAnswerActivity(
            Answer answer
    ) {
        Question question =
                answer.getQuestion();

        Study study =
                question.getStudy();

        return QuestionActivity.builder()
                .activityType(QuestionActivityType.ANSWER)
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

    private QuestionDetailRes createQuestionDetailResponse(
            List<QuestionActivity> activities
    ) {
        boolean hasNext =
                activities.size() > QUESTION_PAGE_SIZE;

        List<QuestionActivity> currentPage =
                activities.stream()
                        .limit(QUESTION_PAGE_SIZE)
                        .toList();

        return QuestionDetailRes.builder()
                .questions(currentPage)
                .pageInfo(
                        createQuestionPageInfo(
                                currentPage,
                                hasNext
                        )
                )
                .build();
    }

    private PageInfo<String> createQuestionPageInfo(
            List<QuestionActivity> activities,
            boolean hasNext
    ) {
        String nextCursor =
                hasNext && !activities.isEmpty()
                        ? createQuestionCursor(
                        activities.get(
                                activities.size() - 1
                        )
                )
                        : null;

        return new PageInfo<>(
                nextCursor,
                activities.size(),
                hasNext
        );
    }

    private String createQuestionCursor(
            QuestionActivity activity
    ) {
        return CursorConverter.encode(
                activity.getCreatedAt(),
                activity.getActivityType(),
                activity.getCursorId()
        );
    }

}