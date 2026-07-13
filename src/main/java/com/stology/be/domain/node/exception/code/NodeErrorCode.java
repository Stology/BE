package com.stology.be.domain.node.exception.code;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NodeErrorCode implements BaseErrorCode {

    // study
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY404_1", "해당 스터디방을 찾을 수 없습니다."),
    STUDY_ALREADY_DELETED(HttpStatus.NOT_FOUND, "STUDY404_2", "이미 삭제된 스터디방입니다."),
    STUDY_ALREADY_CLOSED(HttpStatus.NOT_FOUND, "STUDY404_3", "이미 종료된 스터디입니다."),
    STUDY_INVALID(HttpStatus.BAD_REQUEST, "STUDY400_1", "유효하지 않은 스터디방입니다."),
    STUDY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "STUDY403_1", "해당 스터디방에 접근할 권한이 없습니다."),
    STUDY_LEADER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "STUDY403_2", "스터디장만 접근할 수 있습니다."),
    STUDY_NAME_DUPLICATE(HttpStatus.CONFLICT, "STUDY409_1", "이미 사용 중인 스터디 이름입니다."),

    // template
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE404_1", "해당 템플릿을 찾을 수 없습니다."),

    // reviewer count
    REVIEWER_COUNT_INVALID(HttpStatus.BAD_REQUEST, "REVIEWER400_1", "유효하지 않은 검토 인원수입니다."),
    REVIEWER_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "REVIEWER400_2", "최대 검토 인원수를 초과했습니다."),
    REVIEWER_COUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEWER404_1", "해당 검토 인원수를 찾을 수 없습니다."),

    // Invitation token
    INVITATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "INVITATION400_1", "유효하지 않은 초대 토큰입니다."),
    INVITATION_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITATION404_1", "해당 초대 토큰을 찾을 수 없습니다."),
    INVITATION_ALREADY_JOINED(HttpStatus.CONFLICT, "INVITATION409_1", "이미 초대된 스터디방입니다."),
    INVITATION_TOKEN_ALREADY_EXISTS(HttpStatus.CONFLICT, "INVITATION409_2", "이미 초대 토큰이 생성된 스터디방입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}