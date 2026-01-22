package com.relog.relog.gift.controller;

import com.relog.relog.common.ApiResponse;
import com.relog.relog.gift.dto.GiftCreateRequest;
import com.relog.relog.gift.dto.GiftResponse;
import com.relog.relog.gift.dto.GiftUpdateRequest;
import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import com.relog.relog.gift.service.GiftService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;

    @PostMapping
    public ResponseEntity<ApiResponse<GiftResponse>> createGift(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody GiftCreateRequest request) {
        GiftResponse response = giftService.createGift(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GiftResponse>>> getAllGifts(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) Long friendId,
            @RequestParam(required = false) GiftType type,
            @RequestParam(required = false) GiftDirection direction) {
        List<GiftResponse> responses = getGiftList(memberId, friendId, type, direction);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{giftId}")
    public ResponseEntity<ApiResponse<GiftResponse>> updateGift(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long giftId,
            @RequestBody GiftUpdateRequest request) {
        GiftResponse response = giftService.updateGift(memberId, giftId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{giftId}")
    public ResponseEntity<ApiResponse<Void>> deleteGift(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long giftId) {
        giftService.deleteGift(memberId, giftId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    private List<GiftResponse> getGiftList(Long memberId, Long friendId, GiftType type, GiftDirection direction) {
        if (friendId != null) {
            return giftService.getGiftsByFriend(memberId, friendId);
        }
        if (type != null) {
            return giftService.getGiftsByType(memberId, type);
        }
        if (direction != null) {
            return giftService.getGiftsByDirection(memberId, direction);
        }
        return giftService.getAllGifts(memberId);
    }
}
