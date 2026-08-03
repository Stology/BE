package com.stology.be.global;

public record PageInfo(
        Long nextCursor,
        int size,
        boolean hasNext
) {
}
