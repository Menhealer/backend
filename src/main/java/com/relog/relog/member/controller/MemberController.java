package com.relog.relog.member.controller;

import com.relog.relog.common.ApiResponse;
import com.relog.relog.member.dto.MemberResponse;
import com.relog.relog.member.dto.MemberUpdateRequest;
import com.relog.relog.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyInfo(
            @AuthenticationPrincipal Long memberId) {
        MemberResponse response = memberService.getMember(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMyInfo(
            @AuthenticationPrincipal Long memberId,
            @RequestBody MemberUpdateRequest request) {
        MemberResponse response = memberService.updateMember(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
