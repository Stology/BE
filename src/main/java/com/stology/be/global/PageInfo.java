package com.stology.be.global;

public record PageInfo<T>(
        T nextCursor,
        int size,
        boolean hasNext
) {
}
