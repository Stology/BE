package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.dto.NodeVoteInfoDto;
import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.enums.CandidateState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NodeCandidateRepository extends JpaRepository<NodeCandidate, Long> {

    List<NodeCandidate>
    findByStudyNode_IdAndState(
            Long studyNodeId,
            CandidateState state
    );


    Optional<NodeCandidate>
    findByIdAndStudyNode_IdAndStudyNode_Study_IdAndState(
            Long nodeCandidateId,
            Long studyNodeId,
            Long studyId,
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


    @Query("""
    SELECT new com.stology.be.domain.node.dto.NodeVoteInfoDto(
        nc.id,
        sn.id,
        nc.acceptCount,
        m.id,
        m.name,
        nvi.voteType
    )
    FROM NodeCandidate nc
    JOIN nc.studyNode sn
    JOIN sn.study s
    LEFT JOIN nc.nodeCandidateVoteInfo nvi
    LEFT JOIN nvi.member m
    WHERE s.id = :studyId
      AND nc.state = :state
    ORDER BY nc.id, m.id
    """)
    List<NodeVoteInfoDto> findPendingVoteInfos(
            @Param("studyId") Long studyId,
            @Param("state") CandidateState state
    );

}
