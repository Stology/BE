package com.stology.be.domain.auth.dto;

public record TokenDTO(
        Long userId,
        String accessToken,
        String refreshToken
) { }
