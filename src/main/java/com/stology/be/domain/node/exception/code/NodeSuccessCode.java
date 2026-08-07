package com.stology.be.domain.node.exception.code;

import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NodeSuccessCode implements BaseSuccessCode {

    ACTIVE_NODES_GET_SUCCESS(
            HttpStatus.OK,
            "NODE200_1",
            "활성 노드 조회에 성공했습니다."
    ),

    NODE_INFO_GET_SUCCESS(
            HttpStatus.OK,
            "NODE200_2",
            "노드 상세 정보 조회에 성공했습니다."
    ),

    NODE_VOTE_SUCCESS(
            HttpStatus.OK,
            "NODE200_3",
            "노드 후보 투표 처리에 성공했습니다."
    ),

    EXAMINATION_INFO_GET_SUCCESS(
            HttpStatus.OK,
            "NODE200_4",
            "노드 후보 검토 정보 조회에 성공했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}