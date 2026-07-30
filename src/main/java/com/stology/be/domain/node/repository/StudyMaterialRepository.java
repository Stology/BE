package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.upload.dto.res.RecentFileRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyMaterialRepository extends CrudRepository<StudyMaterial, Long> {

    @Query("""
            SELECT new com.stology.be.domain.upload.dto.res.RecentFileRes(
                s.id,
                m.id,
                m.name,
                sm.dataTitle,
                sm.dataState,
                sm.createdAt
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
}
