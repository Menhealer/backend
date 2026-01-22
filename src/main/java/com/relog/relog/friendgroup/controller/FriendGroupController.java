package com.relog.relog.friendgroup.controller;

import com.relog.relog.common.ApiResponse;
import com.relog.relog.friendgroup.dto.FriendGroupCreateRequest;
import com.relog.relog.friendgroup.dto.FriendGroupResponse;
import com.relog.relog.friendgroup.service.FriendGroupService;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class FriendGroupController {

    private final FriendGroupService friendGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<FriendGroupResponse>> createGroup(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody FriendGroupCreateRequest request) {
        FriendGroupResponse response = friendGroupService.createGroup(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendGroupResponse>>> getAllGroups(
            @AuthenticationPrincipal Long memberId) {
        List<FriendGroupResponse> responses = friendGroupService.getAllGroups(memberId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<FriendGroupResponse>> updateGroup(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long groupId,
            @Valid @RequestBody FriendGroupCreateRequest request) {
        FriendGroupResponse response = friendGroupService.updateGroup(memberId, groupId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long groupId) {
        friendGroupService.deleteGroup(memberId, groupId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
