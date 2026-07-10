package com.stology.be.domain.auth.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) { }
