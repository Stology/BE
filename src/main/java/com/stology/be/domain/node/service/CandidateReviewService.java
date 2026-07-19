package com.stology.be.domain.node.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.dto.CandidateReviewReqDTO;
import com.stology.be.domain.node.dto.CandidateReviewResDTO;
import com.stology.be.domain.node.entity.CandidateReview;
import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.enums.ReviewDecision;
import com.stology.be.domain.node.exception.NodeException;
import com.stology.be.domain.node.exception.code.NodeErrorCode;
import com.stology.be.domain.node.repository.CandidateReviewRepository;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateReviewService {

    private final NodeCandidateRepository nodeCandidateRepository;
    private final CandidateReviewRepository candidateReviewRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final CandidateReviewPolicy candidateReviewPolicy;

    @Transactional
    public CandidateReviewResDTO.Submit submit(
            Long studyId,
            Long materialId,
            Member reviewer,
            CandidateReviewReqDTO.Submit request
    ) {
        List<NodeCandidate> candidates = nodeCandidateRepository.findAllByStudyMaterial_Id(materialId);
        Study study = resolveStudy(studyId, candidates);

        validateReviewer(studyId, reviewer);
        validateStudy(study);
        validatePendingCandidates(candidates);

        Map<Long, ReviewDecision> submittedDecisions = toDecisionMap(request);
        Set<Long> candidateIds = candidates.stream()
                .map(NodeCandidate::getId)
                .collect(Collectors.toSet());
        if (!candidateIds.equals(submittedDecisions.keySet())) {
            throw new NodeException(NodeErrorCode.REVIEW_INCOMPLETE);
        }

        if (candidateReviewRepository
                .existsByNodeCandidate_StudyMaterial_IdAndReviewer_Id(materialId, reviewer.getId())) {
            throw new NodeException(NodeErrorCode.REVIEW_ALREADY_SUBMITTED);
        }

        List<CandidateReview> submittedReviews = candidates.stream()
                .map(candidate -> CandidateReview.builder()
                        .nodeCandidate(candidate)
                        .reviewer(reviewer)
                        .decision(submittedDecisions.get(candidate.getId()))
                        .build())
                .toList();

        List<CandidateReview> allReviews = new ArrayList<>(
                candidateReviewRepository.findAllByNodeCandidate_IdIn(candidateIds));
        allReviews.addAll(submittedReviews);
        candidateReviewRepository.saveAll(submittedReviews);

        int reviewerCount = study.getReviewerCount();
        List<CandidateReviewResDTO.Candidate> results = candidates.stream()
                .map(candidate -> applyReviewResult(candidate, allReviews, reviewerCount))
                .toList();

        return new CandidateReviewResDTO.Submit(materialId, results);
    }

    private Study resolveStudy(Long studyId, List<NodeCandidate> candidates) {
        if (candidates.isEmpty()) {
            throw new NodeException(NodeErrorCode.REVIEW_TARGET_NOT_FOUND);
        }

        boolean belongsToStudy = candidates.stream().allMatch(candidate ->
                candidate.getStudyNode() != null
                        && candidate.getStudyNode().getStudy() != null
                        && Objects.equals(candidate.getStudyNode().getStudy().getId(), studyId));
        if (!belongsToStudy) {
            throw new NodeException(NodeErrorCode.REVIEW_TARGET_NOT_FOUND);
        }
        return candidates.get(0).getStudyNode().getStudy();
    }

    private void validateReviewer(Long studyId, Member reviewer) {
        if (reviewer == null || reviewer.getId() == null
                || !memberStudyRepository.existsByMember_IdAndStudy_Id(reviewer.getId(), studyId)) {
            throw new NodeException(NodeErrorCode.REVIEW_FORBIDDEN);
        }
    }

    private void validateStudy(Study study) {
        if (!Boolean.TRUE.equals(study.getIsActive())) {
            throw new NodeException(NodeErrorCode.REVIEW_INACTIVE_STUDY);
        }
        if (study.getReviewerCount() == null || study.getReviewerCount() < 1) {
            throw new NodeException(NodeErrorCode.REVIEW_INVALID_THRESHOLD);
        }
    }

    private void validatePendingCandidates(List<NodeCandidate> candidates) {
        if (candidates.stream().anyMatch(candidate -> candidate.getState() != CandidateState.PENDING)) {
            throw new NodeException(NodeErrorCode.REVIEW_ALREADY_FINALIZED);
        }
    }

    private Map<Long, ReviewDecision> toDecisionMap(CandidateReviewReqDTO.Submit request) {
        if (request == null || request.decisions() == null) {
            throw new NodeException(NodeErrorCode.REVIEW_INCOMPLETE);
        }

        Map<Long, ReviewDecision> decisions = new HashMap<>();
        for (CandidateReviewReqDTO.Decision decision : request.decisions()) {
            if (decision == null || decision.candidateId() == null || decision.decision() == null
                    || decisions.put(decision.candidateId(), decision.decision()) != null) {
                throw new NodeException(NodeErrorCode.REVIEW_INCOMPLETE);
            }
        }
        return decisions;
    }

    private CandidateReviewResDTO.Candidate applyReviewResult(
            NodeCandidate candidate,
            List<CandidateReview> reviews,
            int reviewerCount
    ) {
        long acceptCount = reviews.stream()
                .filter(review -> Objects.equals(review.getNodeCandidate().getId(), candidate.getId()))
                .filter(review -> review.getDecision() == ReviewDecision.ACCEPT)
                .count();
        long declineCount = reviews.stream()
                .filter(review -> Objects.equals(review.getNodeCandidate().getId(), candidate.getId()))
                .filter(review -> review.getDecision() == ReviewDecision.DECLINE)
                .count();

        CandidateState state = candidateReviewPolicy.decide(
                Math.toIntExact(acceptCount),
                Math.toIntExact(declineCount),
                reviewerCount);
        candidate.updateReviewResult(state, Math.toIntExact(acceptCount));
        return new CandidateReviewResDTO.Candidate(candidate.getId(), state);
    }
}
