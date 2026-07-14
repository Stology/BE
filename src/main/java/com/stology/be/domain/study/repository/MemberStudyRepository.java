package com.stology.be.domain.study.repository;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberStudyRepository extends JpaRepository<MemberStudy, Long> {
    List<MemberStudy> findByMember(Member member);

    Integer countByStudyId(Long id);

    boolean existsByMemberAndStudy(Member member, Study study);


    //존재하는지 id, 기준
    boolean existsByStudyIdAndMemberId(
            Long studyId,
            Long memberId
    );

    Optional<MemberStudy> findByStudyIdAndMemberId(Long studyId, Long memberId);


}
