package com.relog.relog.auth.controller;

import com.relog.relog.auth.dto.EmailCheckRequest;
import com.relog.relog.auth.dto.EmailCheckResponse;
import com.relog.relog.auth.dto.LoginRequest;
import com.relog.relog.auth.dto.PasswordChangeRequest;
import com.relog.relog.auth.dto.SignUpRequest;
import com.relog.relog.auth.dto.TokenResponse;
import com.relog.relog.auth.service.AuthService;
import com.relog.relog.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        TokenResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/email-check")
    public ResponseEntity<ApiResponse<EmailCheckResponse>> checkEmail(@Valid @RequestBody EmailCheckRequest request) {
        boolean isDuplicate = authService.checkEmailDuplicate(request.getEmail());
        EmailCheckResponse response = new EmailCheckResponse(isDuplicate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(memberId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
