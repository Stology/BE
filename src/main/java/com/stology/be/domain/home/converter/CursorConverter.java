package com.stology.be.domain.home.converter;


import com.stology.be.domain.home.enums.QuestionActivityType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public final class CursorConverter {

    private static final String DELIMITER = "\\|";
    private static final String RAW_DELIMITER = "|";
    private static final int CURSOR_FIELD_COUNT = 3;


    //커서에 (작성 시간 | 활동 유형 | ID) 정보 있음.
    private CursorConverter() {
    }

    public static String encode(
            LocalDateTime createdAt,
            QuestionActivityType type,
            Long id
    ) {
        String value = String.join(
                RAW_DELIMITER,
                createdAt.toString(),
                type.name(),
                id.toString()
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8)
                );
    }

    public static CursorValue decode(
            String cursor
    ) {
        if (cursor == null || cursor.isBlank()) {
            return CursorValue.initial();
        }

        try {
            String decodedCursor =
                    new String(
                            Base64.getUrlDecoder().decode(cursor),
                            StandardCharsets.UTF_8
                    );

            String[] values =
                    decodedCursor.split(DELIMITER);

            validateFieldCount(values);

            return new CursorValue(
                    LocalDateTime.parse(values[0]),
                    QuestionActivityType.valueOf(values[1]),
                    Long.parseLong(values[2])
            );
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "잘못된 커서 형식입니다.",
                    exception
            );
        }
    }

    private static void validateFieldCount(
            String[] values
    ) {
        if (values.length != CURSOR_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "잘못된 커서 필드 개수입니다."
            );
        }
    }


    public record CursorValue(
            LocalDateTime createdAt,
            QuestionActivityType type,
            Long id
    ) {
        public static CursorValue initial() {
            return new CursorValue(
                    null,
                    null,
                    null
            );
        }

        public Integer typeRank() {
            return type == null
                    ? null
                    : type.getRank();
        }

        public boolean isInitial() {
            return createdAt == null;
        }

    }
}