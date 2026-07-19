package com.stology.be.domain.node.exception.code;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NodeErrorCode implements BaseErrorCode {
    REVIEW_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND,
            "NODE404_1",
            "검토할 노드 후보를 찾을 수 없습니다."),
    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN,
            "NODE403_1",
            "스터디 멤버만 검토할 수 있습니다."),
    REVIEW_INACTIVE_STUDY(HttpStatus.CONFLICT,
            "NODE409_1",
            "종료된 스터디는 검토할 수 없습니다."),
    REVIEW_ALREADY_FINALIZED(HttpStatus.CONFLICT,
            "NODE409_2",
            "이미 확정된 노드 후보가 포함되어 있습니다."),
    REVIEW_INCOMPLETE(HttpStatus.BAD_REQUEST,
            "NODE400_1",
            "모든 노드 후보를 한 번씩 검토해야 합니다."),
    REVIEW_ALREADY_SUBMITTED(HttpStatus.CONFLICT,
            "NODE409_3",
            "이미 검토를 제출한 자료입니다."),
    REVIEW_INVALID_THRESHOLD(HttpStatus.CONFLICT,
            "NODE409_4",
            "검토 인원 설정이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
