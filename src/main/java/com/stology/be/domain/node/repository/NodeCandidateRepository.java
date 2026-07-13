package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.enums.CandidateState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NodeCandidateRepository extends JpaRepository<NodeCandidate, Long> {

    List<NodeCandidate>
    findByStudyNode_IdAndState(
            Long studyNodeId,
            CandidateState state
    );


    @Query("""
        SELECT nc
        FROM NodeCandidate nc
        JOIN FETCH nc.studyMaterial sm
        JOIN FETCH sm.memberStudy ms
        JOIN FETCH ms.member m
        WHERE nc.studyNode.id = :studyNodeId
          AND nc.studyNode.study.id = :studyId
          AND nc.state = :state
        ORDER BY sm.createdAt DESC
        """)
    List<NodeCandidate> findAcceptedCandidatesWithMaterial(
            @Param("studyId") Long studyId,
            @Param("studyNodeId") Long studyNodeId,
            @Param("state") CandidateState state
    );

}
