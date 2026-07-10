package com.stology.be.domain.member.repository;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialTypeAndSocialUid(SocialType providerId, String socialUid);
    Optional<Member> findBySocialUid(String uid);
}
