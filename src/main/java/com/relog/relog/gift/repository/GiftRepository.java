package com.relog.relog.gift.repository;

import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {

    List<Gift> findAllByMemberId(Long memberId);

    Optional<Gift> findByIdAndMemberId(Long id, Long memberId);

    List<Gift> findAllByMemberIdAndFriendId(Long memberId, Long friendId);

    List<Gift> findAllByMemberIdAndGiftType(Long memberId, GiftType giftType);

    List<Gift> findAllByMemberIdAndDirection(Long memberId, GiftDirection direction);

    @Query("SELECT g FROM Gift g WHERE g.member.id = :memberId AND " +
           "g.giftDate BETWEEN :startDate AND :endDate ORDER BY g.giftDate DESC")
    List<Gift> findAllByMemberIdAndGiftDateBetween(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT g FROM Gift g WHERE g.member.id = :memberId AND g.friend.id = :friendId AND g.giftType = :giftType")
    List<Gift> findAllByMemberIdAndFriendIdAndGiftType(
            @Param("memberId") Long memberId,
            @Param("friendId") Long friendId,
            @Param("giftType") GiftType giftType);

    @Query("SELECT SUM(g.price) FROM Gift g WHERE g.member.id = :memberId AND g.direction = :direction")
    Long sumPriceByMemberIdAndDirection(
            @Param("memberId") Long memberId,
            @Param("direction") GiftDirection direction);

    @Query("SELECT SUM(g.price) FROM Gift g WHERE g.member.id = :memberId AND g.friend.id = :friendId AND g.direction = :direction")
    Long sumPriceByMemberIdAndFriendIdAndDirection(
            @Param("memberId") Long memberId,
            @Param("friendId") Long friendId,
            @Param("direction") GiftDirection direction);
}
