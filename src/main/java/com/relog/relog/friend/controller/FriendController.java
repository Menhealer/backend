package com.relog.relog.friend.controller;

import com.relog.relog.common.ApiResponse;
import com.relog.relog.friend.dto.FriendCreateRequest;
import com.relog.relog.friend.dto.FriendDetailResponse;
import com.relog.relog.friend.dto.FriendNameCheckRequest;
import com.relog.relog.friend.dto.FriendNameCheckResponse;
import com.relog.relog.friend.dto.FriendResponse;
import com.relog.relog.friend.dto.FriendUpdateRequest;
import com.relog.relog.friend.service.FriendService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping
    public ResponseEntity<ApiResponse<FriendResponse>> createFriend(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody FriendCreateRequest request) {
        FriendResponse response = friendService.createFriend(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getAllFriends(
            @AuthenticationPrincipal Long memberId) {
        List<FriendResponse> responses = friendService.getAllFriends(memberId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{friendId}")
    public ResponseEntity<ApiResponse<FriendDetailResponse>> getFriendDetail(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long friendId) {
        FriendDetailResponse response = friendService.getFriendDetail(memberId, friendId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/name-check")
    public ResponseEntity<ApiResponse<FriendNameCheckResponse>> checkName(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody FriendNameCheckRequest request) {
        boolean isDuplicate = friendService.isNameDuplicate(memberId, request.getName());
        FriendNameCheckResponse response = new FriendNameCheckResponse(isDuplicate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{friendId}")
    public ResponseEntity<ApiResponse<FriendResponse>> updateFriend(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long friendId,
            @RequestBody FriendUpdateRequest request) {
        FriendResponse response = friendService.updateFriend(memberId, friendId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long friendId) {
        friendService.deleteFriend(memberId, friendId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}