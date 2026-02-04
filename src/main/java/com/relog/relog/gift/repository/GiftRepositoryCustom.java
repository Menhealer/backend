package com.relog.relog.gift.repository;

import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GiftRepositoryCustom {

    List<Gift> findAllWithFriendByMemberId(Long memberId);

    List<Gift> findAllWithFriendByMemberIdAndFriendId(Long memberId, Long friendId);

    List<Gift> findAllWithFriendByMemberIdAndGiftType(Long memberId, GiftType giftType);

    List<Gift> findAllWithFriendByMemberIdAndDirection(Long memberId, GiftDirection direction);

    List<Gift> findAllWithFriendByMemberIdAndDateRange(Long memberId, LocalDate startDate, LocalDate endDate);

    Optional<Gift> findByIdAndMemberIdWithFriend(Long id, Long memberId);
}
