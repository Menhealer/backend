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
                .itemName(request.getItemName())
                .price(request.getPrice())
                .giftDate(request.getGiftDate())
                .giftType(request.getGiftType())
                .direction(request.getDirection())
                .member(member)
                .friend(friend)
                .build();

        return GiftResponse.from(giftRepository.save(gift));
    }

    public List<GiftResponse> getAllGifts(Long memberId) {
        return giftRepository.findAllByMemberId(memberId).stream()
                .map(GiftResponse::from)
                .toList();
    }

    public List<GiftResponse> getGiftsByFriend(Long memberId, Long friendId) {
        return giftRepository.findAllByMemberIdAndFriendId(memberId, friendId).stream()
                .map(GiftResponse::from)
                .toList();
    }

    public List<GiftResponse> getGiftsByType(Long memberId, GiftType giftType) {
        return giftRepository.findAllByMemberIdAndGiftType(memberId, giftType).stream()
                .map(GiftResponse::from)
                .toList();
    }

    public List<GiftResponse> getGiftsByDirection(Long memberId, GiftDirection direction) {
        return giftRepository.findAllByMemberIdAndDirection(memberId, direction).stream()
                .map(GiftResponse::from)
                .toList();
    }

    @Transactional
    public GiftResponse updateGift(Long memberId, Long giftId, GiftUpdateRequest request) {
        Gift gift = findGiftByIdAndMemberId(giftId, memberId);

        updateItemName(gift, request.getItemName());
        updatePrice(gift, request.getPrice());
        updateGiftDate(gift, request.getGiftDate());
        updateGiftType(gift, request.getGiftType());
        updateDirection(gift, request.getDirection());

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

    private void updateItemName(Gift gift, String itemName) {
        if (itemName == null) {
            return;
        }
        gift.updateItemName(itemName);
    }

    private void updatePrice(Gift gift, Integer price) {
        if (price == null) {
            return;
        }
        gift.updatePrice(price);
    }

    private void updateGiftDate(Gift gift, java.time.LocalDate giftDate) {
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
}
