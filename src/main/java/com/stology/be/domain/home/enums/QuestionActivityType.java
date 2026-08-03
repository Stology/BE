package com.stology.be.domain.home.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionActivityType {

    QUESTION(1),
    ANSWER(0);

    private final int rank;
}