package com.stology.be.domain.study.exception.code;

import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudySuccessCode implements BaseSuccessCode {

    // study
    STUDY_GET_SUCCESS(HttpStatus.OK, "STUDY200_1", "성공적으로 스터디방을 조회했습니다."),
    STUDY_UPDATE_SUCCESS(HttpStatus.OK, "STUDY200_2", "성공적으로 스터디방을 수정했습니다."),
    STUDY_DELETE_SUCCESS(HttpStatus.OK, "STUDY200_3", "성공적으로 스터디방을 삭제했습니다."),
    STUDY_CLOSED_SUCCESS(HttpStatus.OK, "STUDY200_4", "성공적으로 스터디방을 종료했습니다."),
    STUDY_CREATE_SUCCESS(HttpStatus.CREATED, "STUDY201_1", "성공적으로 스터디방을 생성했습니다."),

    // template
    TEMPLATE_GET_SUCCESS(HttpStatus.OK, "TEMPLATE200_1", "성공적으로 템플릿을 조회했습니다."),

    // reviewer count
    REVIEWER_COUNT_GET_SUCCESS(HttpStatus.OK, "REVIEWER200_1", "성공적으로 검토 인원수를 조회했습니다."),
    REVIEWER_COUNT_UPDATE_SUCCESS(HttpStatus.OK, "REVIEWER200_2", "성공적으로 검토 인원수를 수정했습니다."),

    // Invitation token
    INVITATION_TOKEN_GET_SUCCESS(HttpStatus.OK, "INVITATION200_1", "성공적으로 초대 토큰을 조회했습니다."),
    INVITATION_TOKEN_DELETE_SUCCESS(HttpStatus.OK, "INVITATION200_2", "성공적으로 초대 토큰을 삭제했습니다."), // 번호 순서 정렬 (3 -> 2)
    INVITATION_TOKEN_ACCEPT_SUCCESS(HttpStatus.OK, "INVITATION200_3", "성공적으로 스터디 초대를 수락했습니다."), // 네이밍 일관성 및 번호 변경
    INVITATION_TOKEN_CREATE_SUCCESS(HttpStatus.CREATED, "INVITATION201_1", "성공적으로 초대 토큰을 생성했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
