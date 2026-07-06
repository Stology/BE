package com.stology.be.domain.auth.controller;

import com.stology.be.domain.auth.dto.AuthReqDTO;
import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.auth.service.AuthService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Access Token 재발급
    @PostMapping("/reissue")
    public ApiResponse<AuthResDTO.Login> reissue(@RequestBody AuthReqDTO.Reissue request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, authService.reissue(request));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication auth) {
        authService.logout(auth.getName());
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteAccount(Authentication auth) {
        authService.delete(auth.getName());
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }
}
