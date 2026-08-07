package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.upload.dto.res.RecentFileRes;
import com.stology.be.domain.upload.enums.DataState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StudyMaterialRepository extends CrudRepository<StudyMaterial, Long> {

    @Query("""
            SELECT new com.stology.be.domain.upload.dto.res.RecentFileRes(
                s.id,
                m.id,
                m.name,
                sm.dataTitle,
                sm.dataState,
                sm.createdAt,
                sm.id
            )
            FROM StudyMaterial sm
            JOIN sm.memberStudy ms
            JOIN ms.study s
            JOIN ms.member m
            WHERE s.id = :studyId
            ORDER BY sm.createdAt DESC
            """)
    List<RecentFileRes> findRecentFilesByStudyId(
            @Param("studyId") Long studyId,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT sm.memberStudy.study.id
        FROM StudyMaterial sm
        WHERE sm.memberStudy.study.id IN :studyIds
        AND sm.createdAt >= :dateTime
    """)
    List<Long> findNewStudyIds(
            @Param("studyIds") List<Long> studyIds,
            @Param("dateTime") LocalDateTime dateTime
    );

    @Query("""
    SELECT COUNT(sm)
    FROM StudyMaterial sm
    WHERE sm.memberStudy.study.id = :studyId
    AND sm.dataState = com.stology.be.domain.upload.enums.DataState.READY
""")
    Integer countReadyByStudyId(@Param("studyId") Long studyId);

    @Query("""
        SELECT m FROM StudyMaterial m 
        JOIN FETCH m.memberStudy ms 
        JOIN FETCH ms.member 
        WHERE ms.study.id = :studyId 
        AND m.createdAt >= :startOfWeek 
        AND m.createdAt < :endOfWeek
    """)
    List<StudyMaterial> findRecentMaterialsWithMember(
            @Param("studyId") Long studyId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek
    );

    // 자료 집계
    long countByMemberStudyMemberIdAndDataState(
            Long memberId,
            DataState dataState
    );


    // 내 할 일 에서 자료 상세 조회.
    @Query("""
    SELECT sm
    FROM StudyMaterial sm
    JOIN FETCH sm.memberStudy ms
    JOIN FETCH ms.member m
    JOIN FETCH ms.study s
    WHERE m.id = :memberId
      AND sm.dataState IN :dataStates
      AND sm.deletedAt IS NULL
      AND (:cursor IS NULL OR sm.id < :cursor)
    ORDER BY sm.id DESC
""")
    Slice<StudyMaterial> findTodoMaterials(
            @Param("memberId") Long memberId,
            @Param("dataStates") Collection<DataState> dataStates,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
