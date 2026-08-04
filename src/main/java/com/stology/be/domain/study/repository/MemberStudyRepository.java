package com.stology.be.domain.study.repository;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.Study;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberStudyRepository extends JpaRepository<MemberStudy, Long> {
    @EntityGraph(attributePaths = {"study"})
    List<MemberStudy> findByMember(Member member);

    Integer countByStudyId(Long id);

    boolean existsByMemberAndStudy(Member member, Study study);

    //존재하는지 id, 기준
    boolean existsByStudyIdAndMemberId(
            Long studyId,
            Long memberId
    );

    // 스터디 안 모든 맴버들의 id를 가져옵니다.
    @Query("""
    SELECT ms.member
    FROM MemberStudy ms
    WHERE ms.study.id = :studyId
""")
    List<Member> findMembersByStudyId(
            @Param("studyId") Long studyId
    );

    //맴버가 속한 스터디 갯수
    long countByMemberId(Long memberId);

    //맴버가 속한 스터디 리스트
    @Query("""
    SELECT ms.study
    FROM MemberStudy ms
    WHERE ms.member.id = :memberId
      AND (
          :cursor IS NULL
          OR ms.study.id < :cursor
      )
    ORDER BY ms.study.id DESC
""")
    Slice<Study> findReportStudiesByMemberId(
            @Param("memberId") Long memberId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );


    Optional<MemberStudy> findByStudyIdAndMemberId(Long studyId, Long memberId);

    @org.springframework.data.jpa.repository.Query("SELECT ms.member.name FROM MemberStudy ms WHERE ms.study.id = :studyId")
    List<String> findMemberNamesByStudyId(@org.springframework.data.repository.query.Param("studyId") Long studyId);
}
