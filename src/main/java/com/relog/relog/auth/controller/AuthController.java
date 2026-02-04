package com.relog.relog.auth.controller;

import com.relog.relog.auth.dto.SocialLoginRequest;
import com.relog.relog.auth.dto.SocialLoginResponse;
import com.relog.relog.auth.dto.SocialSignUpRequest;
import com.relog.relog.auth.dto.TokenRefreshRequest;
import com.relog.relog.auth.dto.TokenResponse;
import com.relog.relog.auth.service.AuthService;
import com.relog.relog.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> socialLogin(
            @Valid @RequestBody SocialLoginRequest request) {
        SocialLoginResponse response = authService.socialLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signUp(
            @Valid @RequestBody SocialSignUpRequest request) {
        TokenResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {
        TokenResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long memberId) {
        authService.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
