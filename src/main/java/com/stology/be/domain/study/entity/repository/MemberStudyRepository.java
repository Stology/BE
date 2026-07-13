package com.stology.be.domain.study.entity.repository;

import com.stology.be.domain.study.entity.MemberStudy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberStudyRepository extends JpaRepository<MemberStudy, Long> {

    Optional<MemberStudy> findByStudyIdAndMemberId(Long studyId,Long memberId);

}
