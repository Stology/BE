package com.stology.be.domain.report.repository;

import com.stology.be.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByStudyIdOrderByCreatedAtAsc(Long studyId);
    Optional<Report> findByIdAndStudyId(Long id, Long studyId);


    //각 스터디들의 최신 리포트 가져오기
    @Query("""
    SELECT r
    FROM Report r
    JOIN FETCH r.study s
    WHERE s.id IN :studyIds
      AND NOT EXISTS (
          SELECT r2.id
          FROM Report r2
          WHERE r2.study.id = s.id
            AND (
                r2.createdAt > r.createdAt
                OR (
                    r2.createdAt = r.createdAt
                    AND r2.id > r.id
                )
            )
      )
    ORDER BY s.createdAt DESC
""")
    List<Report> findLatestByStudyIds(
            @Param("studyIds") List<Long> studyIds
    );
}
