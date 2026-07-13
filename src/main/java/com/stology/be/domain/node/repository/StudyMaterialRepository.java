package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.upload.dto.res.RecentFileRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyMaterialRepository extends CrudRepository<StudyMaterial, Long> {

    @Query("""
            SELECT new com.stology.be.domain.upload.dto.res.RecentFileRes(
                s.id,
                m.id,
                m.name,
                sm.dataTitle,
                sm.week,
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

}
