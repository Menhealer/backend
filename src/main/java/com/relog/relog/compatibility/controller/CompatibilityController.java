package com.relog.relog.compatibility.controller;

import com.relog.relog.common.ApiResponse;
import com.relog.relog.compatibility.dto.CompatibilityRequest;
import com.relog.relog.compatibility.dto.CompatibilityResponse;
import com.relog.relog.compatibility.service.CompatibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compatibility")
@RequiredArgsConstructor
public class CompatibilityController {

    private final CompatibilityService compatibilityService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompatibilityResponse>> analyzeCompatibility(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CompatibilityRequest request) {
        CompatibilityResponse response = compatibilityService.analyze(memberId, request.getFriendId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
