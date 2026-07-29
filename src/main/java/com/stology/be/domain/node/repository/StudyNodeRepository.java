package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.study.entity.MemberStudy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudyNodeRepository extends JpaRepository<StudyNode, Long> {
    Optional<StudyNode> findTopByStudyIdOrderByUpdatedAtDesc(Long studyId);
    Optional<StudyNode> findByIdAndStudyId(Long studyNodeId, Long studyId);

    //존재 여부만 검사
    boolean existsByIdAndStudyId(
            Long studyNodeId,
            Long studyId
    );

    boolean existsByStudyId(Long studyId);


    List<StudyNode>
    findByStudy_IdAndActivationWeekAndActiveLevelGreaterThanEqualOrderByActiveLevelAsc(
            Long studyId,
            Integer activationWeek,
            Integer minActiveLevel
    );

    long deleteByIdIn(
            Collection<Long> studyNodeIds
    );


    Long countByStudy_Id(Long studyId);

    //투표 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT sn
        FROM StudyNode sn
        WHERE sn.id = :studyNodeId
    """)
    Optional<StudyNode> findByIdForUpdate(
            @Param("studyNodeId") Long studyNodeId
    );

}
