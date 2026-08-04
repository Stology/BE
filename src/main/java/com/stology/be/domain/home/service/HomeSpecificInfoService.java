package com.stology.be.domain.home.service;

import com.stology.be.domain.home.dto.res.AnswerDetailRes;
import com.stology.be.domain.home.dto.res.MaterialDetailRes;
import com.stology.be.domain.home.dto.res.QuestionDetailRes;
import com.stology.be.domain.home.dto.res.ReportDetailRes;
import com.stology.be.domain.inquiry.enums.InquiryStatus;
import com.stology.be.domain.inquiry.repository.InquiryReadRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.report.entity.Report;
import com.stology.be.domain.report.repository.ReportRepository;
import com.stology.be.domain.study.entity.*;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.domain.study.repository.MemberStudyRepository;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeSpecificInfoService {

    private static final int ACTIVITY_PAGE_SIZE = 10;


    private final StudyMaterialRepository studyMaterialRepository;
    private final InquiryReadRepository inquiryReadRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final ReportRepository reportRepository;

    public MaterialDetailRes getMaterialDetail(
            Long memberId,
            Long cursor
    ) {

        Slice<StudyMaterial> materialSlice =
                findMaterials(
                        memberId,
                        cursor,
                        ACTIVITY_PAGE_SIZE
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

    public ReportDetailRes getReportDetail(
            Long memberId,
            Long cursor
    ) {

        Slice<Study> studySlice =
                memberStudyRepository
                        .findReportStudiesByMemberId(
                                memberId,
                                cursor,
                                PageRequest.of(
                                        0,
                                        ACTIVITY_PAGE_SIZE
                                )
                        );
        //첫 요청에 스터디가 있는지 검증
        validateReportStudies(studySlice, cursor);

        List<Study> studies =
                studySlice.getContent();

        // 다음 페이지를 잘못 추가 요청했는데 더 이상 스터디가 없는 경우
        if (studies.isEmpty()) {
            return createEmptyReportDetail();
        }

        Map<Long, Report> latestReportMap =
                getLatestReportMap(studies);


        return createReportDetailResponse(
                studySlice,
                latestReportMap
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
                .week(calculateWeek(study.getCreatedAt().toLocalDate(), material.getCreatedAt().toLocalDate()))
                .uploaderName(memberStudy.getMember().getName())
                .uploadedDate(material.getCreatedAt().toLocalDate())
                .build();
    }


    // 주차계산
    private long calculateWeek(
            LocalDate startDate,
            LocalDate compareDate
    ) {
        long elapsedDays = ChronoUnit.DAYS.between(
                startDate
                ,compareDate
        );

        return Math.max(
                1L,
                elapsedDays / 7L + 1L
        );
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

    //리포트 관련 내부 매서드

    private void validateReportStudies(
            Slice<Study> studySlice,
            Long cursor
    ) {
        if (cursor == null && studySlice.isEmpty()) {
            throw new StudyException(
                    StudyErrorCode.STUDY_NOT_FOUND
            );
        }
    }

    private Map<Long, Report> getLatestReportMap(
            List<Study> studies
    ) {
        List<Long> studyIds =
                studies.stream()
                        .map(Study::getId)
                        .toList();

        return reportRepository
                .findLatestByStudyIds(studyIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                report -> report
                                        .getStudy()
                                        .getId(),
                                Function.identity()
                        )
                );
    }

    private ReportDetailRes.ReportInfo toReportInfo(
            Study study,
            Report latestReport
    ) {
        if (latestReport == null) {
            return createBeforeGenerationReport(study);
        }

        return createCompletedReport(
                study,
                latestReport
        );
    }

    private ReportDetailRes.ReportInfo createBeforeGenerationReport(
            Study study
    ) {
        return ReportDetailRes.ReportInfo.builder()
                .studyId(study.getId())
                .studyName(study.getName())
                .reportId(null)
                .reportWeek(null)
                .createdAt(null)
                .generated(false)
                .build();
    }

    private ReportDetailRes.ReportInfo createCompletedReport(
            Study study,
            Report latestReport
    ) {


        return ReportDetailRes.ReportInfo.builder()
                .studyId(study.getId())
                .studyName(study.getName())
                .reportId(latestReport.getId())
                .reportWeek(calculateWeek(study.getStartDate().toLocalDate(), latestReport.getCreatedAt().toLocalDate()))
                .createdAt(latestReport.getCreatedAt())
                .generated(true)
                .build();
    }


    private ReportDetailRes createReportDetailResponse(
            Slice<Study> studySlice,
            Map<Long, Report> latestReportMap
    ) {
        List<Study> studies =
                studySlice.getContent();

        List<ReportDetailRes.ReportInfo> reportInfos =
                studies.stream()
                        .map(study -> toReportInfo(
                                study,
                                latestReportMap.get(study.getId())
                        ))
                        .toList();

        Long nextCursor =
                studySlice.hasNext() && !studies.isEmpty()
                        ? studies.get(studies.size() - 1).getId()
                        : null;

        return ReportDetailRes.builder()
                .reports(reportInfos)
                .pageInfo(
                        new PageInfo<>(
                                nextCursor,
                                reportInfos.size(),
                                studySlice.hasNext()
                        )
                )
                .build();
    }

    private ReportDetailRes createEmptyReportDetail() {
        return ReportDetailRes.builder()
                .reports(List.of())
                .pageInfo(
                        new PageInfo<>(
                                null,
                                0,
                                false
                        )
                )
                .build();
    }


}