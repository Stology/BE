package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.study.entity.MemberStudy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    Integer countByStudy_IdAndActiveLevelGreaterThan(Long studyId, Integer activeLevel);

    List<StudyNode> findByStudy_IdAndRecommendWeek(Long studyId, Integer recommendWeek);

    @Query("""
        SELECT DISTINCT n FROM StudyNode n
        JOIN NodeCandidate nc ON nc.studyNode.id = n.id
        JOIN nc.studyMaterial m
        WHERE m.createdAt >= :startOfWeek AND m.createdAt < :endOfWeek AND n.study.id = :studyId
    """)
    List<StudyNode> findActiveNodesBetween(
            @Param("studyId") Long studyId,
            @Param("startOfWeek") java.time.LocalDateTime startOfWeek,
            @Param("endOfWeek") java.time.LocalDateTime endOfWeek
    );


    // 스터디안 최신(cutoff) 활성화 노드 리스트 찾기.
    @Query("""
    SELECT sn
    FROM StudyNode sn
    JOIN FETCH sn.study s
    WHERE s.id IN :studyIds
      AND sn.activatedAt >= :cutoff
      AND sn.activatedAt <= :now
    ORDER BY sn.activatedAt DESC, sn.id DESC
""")
    List<StudyNode> findRecentActivatedNodes(
            @Param("studyIds") List<Long> studyIds,
            @Param("cutoff") LocalDateTime cutoff,
            @Param("now") LocalDateTime now
    );
}
