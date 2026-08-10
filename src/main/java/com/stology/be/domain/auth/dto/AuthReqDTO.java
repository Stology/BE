package com.stology.be.domain.auth.dto;

public class AuthReqDTO {
    public record TestRequest(
            String secretCode
    ) {}
}
