package com.stology.be.global.security.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.domain.member.exception.MemberException;
import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final MemberRepository memberRepository;

    public UserDetails loadUserByUidAndSocialType(
            SocialType socialType,
            String username
    ) throws UsernameNotFoundException {
        Member member = memberRepository.findBySocialTypeAndSocialUid(socialType, username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        return new AuthMember(member);
    }
}
