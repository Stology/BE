package com.stology.be.domain.study.repository;

import com.stology.be.domain.study.entity.MemberStudy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberStudyRepository extends JpaRepository<MemberStudy, Long> {

    boolean existsByMember_IdAndStudy_Id(Long memberId, Long studyId);
}
