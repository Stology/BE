package com.stology.be.domain.member.converter;

import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.global.security.dto.OAuthDTO;

public class MemberConverter {
    public static Member toMember(OAuthDTO dto) {
        return Member.builder()
                .name(dto.getName())
                .socialType(dto.getSocialType())
                .socialUid(dto.getSocialUid())
                .email(dto.getSocialEmail())
                .build();
    }

    public static AuthResDTO.Login toLogin(String accessToken, String refreshToken) {
        return new AuthResDTO.Login(
                accessToken,
                refreshToken
        );
    }
}