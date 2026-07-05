package com.stology.be.domain.member.exception.code;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    MEMBER_BAD_REQUEST(HttpStatus.BAD_REQUEST,
            "MEMBER400_1",
            "잘못된 정보입니다."),
    MEMBER_NOT_SUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST,
            "MEMBER400_2",
            "지원되지 않는 소셜 로그인 유형입니다."),
    MEMBER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "MEMBER401_1",
            "이메일 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "MEMBER404_1",
            "유저를 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT,
            "MEMBER409_1",
            "이미 존재하는 유저입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
