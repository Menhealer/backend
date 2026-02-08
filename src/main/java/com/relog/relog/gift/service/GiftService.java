package com.relog.relog.gift.service;

import com.relog.relog.friend.entity.Friend;
import com.relog.relog.friend.exception.FriendNotFoundException;
import com.relog.relog.friend.repository.FriendRepository;
import com.relog.relog.gift.dto.GiftCreateRequest;
import com.relog.relog.gift.dto.GiftResponse;
import com.relog.relog.gift.dto.GiftUpdateRequest;
import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import com.relog.relog.gift.exception.GiftNotFoundException;
import com.relog.relog.gift.repository.GiftRepository;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiftService {

    private final GiftRepository giftRepository;
    private final RelogMemberRepository memberRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public GiftResponse createGift(Long memberId, GiftCreateRequest request) {
        RelogMember member = findMemberById(memberId);
        Friend friend = findFriendByIdAndMemberId(request.getFriendId(), memberId);

        Gift gift = Gift.builder()
                .price(request.getPrice())
                .giftDate(request.getGiftDate())
                .giftType(request.getGiftType())
                .direction(request.getDirection())
                .description(request.getDescription())
                .member(member)
                .friend(friend)
                .build();

        return GiftResponse.from(giftRepository.save(gift));
    }

    public List<GiftResponse> getAllGifts(Long memberId) {
        return giftRepository.findAllWithFriendByMemberId(memberId).stream()
                .map(GiftResponse::from)
                .toList();
    }

    public List<GiftResponse> getGiftsByFriend(Long memberId, Long friendId) {
        return giftRepository.findAllWithFriendByMemberIdAndFriendId(memberId, friendId).stream()
                .map(GiftResponse::from)
                .toList();
    }

    public List<GiftResponse> getGiftsByType(Long memberId, GiftType giftType) {
        return giftRepository.findAllWithFriendByMemberIdAndGiftType(memberId, giftType).stream()
                .map(GiftResponse::from)
                .toList();
    }

    public List<GiftResponse> getGiftsByDirection(Long memberId, GiftDirection direction) {
        return giftRepository.findAllWithFriendByMemberIdAndDirection(memberId, direction).stream()
                .map(GiftResponse::from)
                .toList();
    }

    @Transactional
    public GiftResponse updateGift(Long memberId, Long giftId, GiftUpdateRequest request) {
        Gift gift = findGiftByIdAndMemberId(giftId, memberId);

        updateFriend(gift, request.getFriendId(), memberId);
        updatePrice(gift, request.getPrice());
        updateGiftDate(gift, request.getGiftDate());
        updateGiftType(gift, request.getGiftType());
        updateDirection(gift, request.getDirection());
        updateDescription(gift, request.getDescription());

        return GiftResponse.from(gift);
    }

    @Transactional
    public void deleteGift(Long memberId, Long giftId) {
        Gift gift = findGiftByIdAndMemberId(giftId, memberId);
        giftRepository.delete(gift);
    }

    private RelogMember findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private Friend findFriendByIdAndMemberId(Long friendId, Long memberId) {
        return friendRepository.findByIdAndMemberId(friendId, memberId)
                .orElseThrow(FriendNotFoundException::new);
    }

    private Gift findGiftByIdAndMemberId(Long giftId, Long memberId) {
        return giftRepository.findByIdAndMemberId(giftId, memberId)
                .orElseThrow(GiftNotFoundException::new);
    }

    private void updateFriend(Gift gift, Long friendId, Long memberId) {
        if (friendId == null) {
            return;
        }
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);
        gift.updateFriend(friend);
    }

    private void updatePrice(Gift gift, Integer price) {
        if (price == null) {
            return;
        }
        gift.updatePrice(price);
    }

    private void updateGiftDate(Gift gift, LocalDate giftDate) {
        if (giftDate == null) {
            return;
        }
        gift.updateGiftDate(giftDate);
    }

    private void updateGiftType(Gift gift, GiftType giftType) {
        if (giftType == null) {
            return;
        }
        gift.updateGiftType(giftType);
    }

    private void updateDirection(Gift gift, GiftDirection direction) {
        if (direction == null) {
            return;
        }
        gift.updateDirection(direction);
    }

    private void updateDescription(Gift gift, String description) {
        if (description == null) {
            return;
        }
        gift.updateDescription(description);
    }
}
