package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.dto.NodeVoteInfoDto;
import com.stology.be.domain.node.entity.NodeCandidateVoteInfo;
import com.stology.be.domain.node.enums.CandidateState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface NodeCandidateVoteInfoRepository extends JpaRepository<NodeCandidateVoteInfo, Long> {


    @Query("""
    SELECT new com.stology.be.domain.node.dto.NodeVoteInfoDto(
        nc.id,
        sn.id,
        nc.acceptCount,
        m.id,
        m.name,
        nvi.voteType
    )
    FROM NodeCandidateVoteInfo nvi
    JOIN nvi.nodeCandidate nc
    JOIN nc.studyNode sn
    JOIN sn.study s
    JOIN nvi.member m
    WHERE s.id = :studyId
      AND nc.state = :state
    ORDER BY nc.id, m.id
""")
    List<NodeVoteInfoDto> findPendingVoteInfos(
            @Param("studyId") Long studyId,
            @Param("state") CandidateState state
    );

    //노드 후보 id와 맴버 id로 투표 정보 찾기
    Optional<NodeCandidateVoteInfo>
    findByNodeCandidate_IdAndMember_Id(
            Long nodeCandidateId,
            Long memberId
    );

}
