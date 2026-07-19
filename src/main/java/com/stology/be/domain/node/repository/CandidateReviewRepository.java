package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.CandidateReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CandidateReviewRepository extends JpaRepository<CandidateReview, Long> {

    boolean existsByNodeCandidate_StudyMaterial_IdAndReviewer_Id(Long materialId, Long reviewerId);

    List<CandidateReview> findAllByNodeCandidate_IdIn(Collection<Long> candidateIds);
}
