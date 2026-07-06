package com.stology.be.domain.auth.dto;

public class AuthResDTO {
    public record Login(
            String accessToken,
            String refreshToken
    ) {}
}
