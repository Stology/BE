package com.stology.be.domain.home.service;

import com.stology.be.domain.home.converter.CursorConverter;
import com.stology.be.domain.home.converter.HomeConverter;
import com.stology.be.domain.home.dto.NodeActivityKey;
import com.stology.be.domain.home.dto.res.MyTodoRes;
import com.stology.be.domain.home.dto.res.TeamActivityRes;
import com.stology.be.domain.home.enums.TeamActivityType;
import com.stology.be.domain.inquiry.enums.InquiryStatus;
import com.stology.be.domain.inquiry.repository.InquiryReadRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.global.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeInfoService {

    private static final long TEAM_ACTIVITY_DAYS = 7L;
    private static final int DEFAULT_TEAM_ACTIVITY_SIZE = 10;

    private final StudyNodeRepository studyNodeRepository;
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


    public TeamActivityRes getTeamTodos(
            Long studyId,
            String cursor,
            Long memberId
    ) {
        //커서 정보 얻기
        CursorConverter.CursorValue cursorValue =
                CursorConverter.decode(cursor);
        // 할일 찾을 스터디 찾기
        List<Study> studies =
                getTargetStudies(
                        studyId,
                        memberId
                );
        // 지금 시간, 최근을 정의한 시간
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(TEAM_ACTIVITY_DAYS);

        List<Long> studyIds =
                studies.stream()
                        .map(Study::getId)
                        .toList();

        // 팀 내 최근 활성화 노드 정보 얻기.
        List<TeamActivityRes.TeamActivityInfo> nodeActivities =
                getNodeActivities(
                        studyIds,
                        cutoff,
                        now
                );

        // 팀 내 최근 답변 얻기
        List<TeamActivityRes.TeamActivityInfo> answerActivities =
                getAnswerActivities(
                        studyIds,
                        cutoff,
                        now
                );

        // 팀 활동 res 작성.
        List<TeamActivityRes.TeamActivityInfo> activities =
                HomeConverter.mergeActivities(
                        nodeActivities,
                        answerActivities,
                        cursorValue,
                        DEFAULT_TEAM_ACTIVITY_SIZE
                );

        return createTeamActivityResponse(
                activities,
                DEFAULT_TEAM_ACTIVITY_SIZE
        );
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



    //조회 대상 스터디 결정
    private List<Study> getTargetStudies(
            Long studyId,
            Long memberId
    ) {
        if (studyId == -1L) {
            List<Study> studies =
                    memberStudyRepository
                            .findStudiesByMemberId(memberId);

            if (studies.isEmpty()) {
                throw new StudyException(
                        StudyErrorCode.STUDY_NOT_FOUND
                );
            }

            return studies;
        }

        Study study =
                memberStudyRepository
                        .findByStudyIdAndMemberId(
                                studyId,
                                memberId
                        )
                        .map(MemberStudy::getStudy)
                        .orElseThrow(() ->
                                new StudyException(
                                        StudyErrorCode.STUDY_ACCESS_DENIED
                                )
                        );

        return List.of(study);
    }

    //노드 활동 조회 및 그룹핑
    private List<TeamActivityRes.TeamActivityInfo> getNodeActivities(
            List<Long> studyIds,
            LocalDateTime cutoff,
            LocalDateTime now
    ) {
        List<StudyNode> studyNodes =
                studyNodeRepository.findRecentActivatedNodes(
                        studyIds,
                        cutoff,
                        now
                );

        Map<NodeActivityKey, List<StudyNode>> groupedNodes =
                groupNodes(studyNodes);

        return groupedNodes.entrySet().stream()
                .map(entry -> {
                    NodeActivityKey key =
                            entry.getKey();

                    List<StudyNode> nodes =
                            entry.getValue();

                    Study study =
                            nodes.get(0).getStudy();

                    Long cursorId =
                            nodes.stream()
                                    .map(StudyNode::getId)
                                    .max(Long::compareTo)
                                    .orElseThrow();

                    return TeamActivityRes.TeamActivityInfo.builder()
                            .activityType(TeamActivityType.NODE)
                            .studyId(study.getId())
                            .studyName(study.getName())
                            .targetId(study.getId())
                            .event(
                                    "새 개념 "
                                            + nodes.size()
                                            + "개 반영"
                            )
                            .occurredAt(key.occurredAt())
                            .cursorId(cursorId)
                            .build();
                })
                .toList();
    }



    private Map<NodeActivityKey, List<StudyNode>> groupNodes(
            List<StudyNode> studyNodes
    ) {
        return studyNodes.stream()
                .collect(
                        Collectors.groupingBy(
                                node -> new NodeActivityKey(
                                        node.getStudy().getId(),
                                        node.getActivatedAt()
                                                .truncatedTo(
                                                        ChronoUnit.MINUTES
                                                )
                                )
                        )
                );
    }

    private List<TeamActivityRes.TeamActivityInfo> getAnswerActivities(
            List<Long> studyIds,
            LocalDateTime cutoff,
            LocalDateTime now
    ) {
        return inquiryReplyRepository
                .findRecentTeamAnswers(
                        studyIds,
                        cutoff,
                        now
                )
                .stream()
                .map(answer -> {
                    Question question =
                            answer.getQuestion();

                    Study study =
                            question.getStudy();

                    return TeamActivityRes.TeamActivityInfo.builder()
                            .activityType(TeamActivityType.ANSWER)
                            .studyId(study.getId())
                            .studyName(study.getName())
                            .targetId(question.getId())
                            .event(
                                    answer.getMemberName()
                                            + "님이 답글 등록"
                            )
                            .occurredAt(answer.getCreatedAt())
                            .cursorId(answer.getId())
                            .build();
                })
                .toList();
    }



    private TeamActivityRes createTeamActivityResponse(
            List<TeamActivityRes.TeamActivityInfo> activities,
            int pageSize
    ) {
        boolean hasNext =
                activities.size() > pageSize;

        List<TeamActivityRes.TeamActivityInfo> currentPage =
                activities.stream()
                        .limit(pageSize)
                        .toList();

        String nextCursor =
                hasNext && !currentPage.isEmpty()
                        ? createTeamActivityCursor(
                        currentPage.get(
                                currentPage.size() - 1
                        )
                )
                        : null;

        return TeamActivityRes.builder()
                .activities(currentPage)
                .pageInfo(
                        new PageInfo<>(
                                nextCursor,
                                currentPage.size(),
                                hasNext
                        )
                )
                .build();
    }
    private String createTeamActivityCursor(
            TeamActivityRes.TeamActivityInfo activity
    ) {
        return CursorConverter.encode(
                activity.getOccurredAt(),
                activity.getActivityType(),
                activity.getCursorId()
        );
    }

}