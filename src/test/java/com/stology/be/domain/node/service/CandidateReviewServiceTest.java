package com.stology.be.domain.node.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.dto.CandidateReviewReqDTO;
import com.stology.be.domain.node.dto.CandidateReviewResDTO;
import com.stology.be.domain.node.entity.CandidateReview;
import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.enums.ReviewDecision;
import com.stology.be.domain.node.exception.NodeException;
import com.stology.be.domain.node.repository.CandidateReviewRepository;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CandidateReviewServiceTest {

    private final NodeCandidateRepository nodeCandidateRepository = mock(NodeCandidateRepository.class);
    private final CandidateReviewRepository candidateReviewRepository = mock(CandidateReviewRepository.class);
    private final MemberStudyRepository memberStudyRepository = mock(MemberStudyRepository.class);
    private final CandidateReviewService service = new CandidateReviewService(
            nodeCandidateRepository,
            candidateReviewRepository,
            memberStudyRepository,
            new CandidateReviewPolicy()
    );

    private Member reviewer;
    private Study study;
    private NodeCandidate candidate;

    @BeforeEach
    void setUp() {
        reviewer = Member.builder().id(1L).name("reviewer").build();
        study = Study.builder().id(10L).reviewerCount(2).build();
        StudyMaterial material = StudyMaterial.builder().id(20L).build();
        StudyNode studyNode = StudyNode.builder().id(30L).study(study).build();
        candidate = NodeCandidate.builder()
                .id(100L)
                .studyMaterial(material)
                .studyNode(studyNode)
                .build();
    }

    @Test
    void submitsCompleteReviewAndAcceptsCandidateAtThreshold() {
        Member previousReviewer = Member.builder().id(2L).name("previous").build();
        CandidateReview previousReview = CandidateReview.builder()
                .nodeCandidate(candidate)
                .reviewer(previousReviewer)
                .decision(ReviewDecision.ACCEPT)
                .build();
        CandidateReviewReqDTO.Submit request = requestFor(candidate.getId(), ReviewDecision.ACCEPT);

        when(memberStudyRepository.existsByMember_IdAndStudy_Id(reviewer.getId(), study.getId()))
                .thenReturn(true);
        when(nodeCandidateRepository.findAllByStudyMaterial_Id(20L)).thenReturn(List.of(candidate));
        when(candidateReviewRepository.existsByNodeCandidate_StudyMaterial_IdAndReviewer_Id(20L, reviewer.getId()))
                .thenReturn(false);
        when(candidateReviewRepository.findAllByNodeCandidate_IdIn(any()))
                .thenReturn(List.of(previousReview));

        CandidateReviewResDTO.Submit response = service.submit(10L, 20L, reviewer, request);

        assertEquals(CandidateState.ACCEPT, candidate.getState());
        assertEquals(CandidateState.ACCEPT, response.candidates().get(0).state());
        verify(candidateReviewRepository).saveAll(anyList());
    }

    @Test
    void rejectsReviewerWhoIsNotAStudyMember() {
        when(nodeCandidateRepository.findAllByStudyMaterial_Id(20L)).thenReturn(List.of(candidate));
        when(memberStudyRepository.existsByMember_IdAndStudy_Id(reviewer.getId(), study.getId()))
                .thenReturn(false);

        assertThrows(NodeException.class,
                () -> service.submit(10L, 20L, reviewer, requestFor(100L, ReviewDecision.ACCEPT)));
    }

    @Test
    void rejectsSubmissionThatOmitsAMaterialCandidate() {
        StudyMaterial material = candidate.getStudyMaterial();
        NodeCandidate secondCandidate = NodeCandidate.builder()
                .id(101L)
                .studyMaterial(material)
                .studyNode(candidate.getStudyNode())
                .build();
        when(nodeCandidateRepository.findAllByStudyMaterial_Id(20L))
                .thenReturn(List.of(candidate, secondCandidate));
        when(memberStudyRepository.existsByMember_IdAndStudy_Id(reviewer.getId(), study.getId()))
                .thenReturn(true);

        assertThrows(NodeException.class,
                () -> service.submit(10L, 20L, reviewer, requestFor(100L, ReviewDecision.ACCEPT)));
    }

    @Test
    void rejectsRepeatSubmissionForTheSameMaterial() {
        when(nodeCandidateRepository.findAllByStudyMaterial_Id(20L)).thenReturn(List.of(candidate));
        when(memberStudyRepository.existsByMember_IdAndStudy_Id(reviewer.getId(), study.getId()))
                .thenReturn(true);
        when(candidateReviewRepository.existsByNodeCandidate_StudyMaterial_IdAndReviewer_Id(20L, reviewer.getId()))
                .thenReturn(true);

        assertThrows(NodeException.class,
                () -> service.submit(10L, 20L, reviewer, requestFor(100L, ReviewDecision.ACCEPT)));
    }

    private CandidateReviewReqDTO.Submit requestFor(Long candidateId, ReviewDecision decision) {
        return new CandidateReviewReqDTO.Submit(List.of(
                new CandidateReviewReqDTO.Decision(candidateId, decision)
        ));
    }
}
