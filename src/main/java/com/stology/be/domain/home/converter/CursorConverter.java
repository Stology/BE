package com.stology.be.domain.home.converter;

import com.stology.be.domain.home.enums.TeamActivityType;
import com.stology.be.domain.home.exception.HomeException;
import com.stology.be.global.apiPayload.code.GeneralErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public final class CursorConverter {

    private static final String RAW_DELIMITER = "|";
    private static final String DELIMITER_REGEX = "\\|";
    private static final int CURSOR_FIELD_COUNT = 3;

    private CursorConverter() {
    }

    public static String encode(
            LocalDateTime occurredAt,
            TeamActivityType type,
            Long id
    ) {
        String rawCursor = String.join(
                RAW_DELIMITER,
                occurredAt.toString(),
                type.name(),
                id.toString()
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        rawCursor.getBytes(StandardCharsets.UTF_8)
                );
    }

    public static CursorValue decode(
            String cursor
    ) {
        if (cursor == null || cursor.isBlank()) {
            return CursorValue.initial();
        }

        try {
            String rawCursor = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );

            String[] fields = rawCursor.split(
                    DELIMITER_REGEX,
                    -1
            );

            validateFieldCount(fields);

            return new CursorValue(
                    LocalDateTime.parse(fields[0]),
                    TeamActivityType.valueOf(fields[1]),
                    Long.parseLong(fields[2])
            );
        } catch (RuntimeException exception) {
            throw new GeneralException(
                    GeneralErrorCode.BAD_REQUEST
            );
        }
    }

    private static void validateFieldCount(
            String[] fields
    ) {
        if (fields.length != CURSOR_FIELD_COUNT) {
            throw new HomeException(HomeErrorCode.CURSOR_INVALID);
        }
    }

    public static boolean isAfterCursor(
            LocalDateTime occurredAt,
            TeamActivityType type,
            Long id,
            CursorValue cursor
    ) {
        if (cursor.isInitial()) {
            return true;
        }

        int timeComparison =
                occurredAt.compareTo(
                        cursor.occurredAt()
                );

        if (timeComparison != 0) {
            return timeComparison < 0;
        }

        int typeRank =
                type.getRank();

        if (typeRank != cursor.typeRank()) {
            return typeRank < cursor.typeRank();
        }

        return id < cursor.id();
    }



    public record CursorValue(
            LocalDateTime occurredAt,
            TeamActivityType type,
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
            return occurredAt == null;
        }
    }
}
