package com.relog.relog.gift.repository;

import static com.relog.relog.friend.entity.QFriend.friend;
import static com.relog.relog.gift.entity.QGift.gift;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GiftRepositoryImpl implements GiftRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Gift> findAllWithFriendByMemberId(Long memberId) {
        return queryFactory
                .selectFrom(gift)
                .join(gift.friend, friend).fetchJoin()
                .where(gift.member.id.eq(memberId))
                .orderBy(gift.giftDate.desc())
                .fetch();
    }

    @Override
    public List<Gift> findAllWithFriendByMemberIdAndFriendId(Long memberId, Long friendId) {
        return queryFactory
                .selectFrom(gift)
                .join(gift.friend, friend).fetchJoin()
                .where(
                        gift.member.id.eq(memberId),
                        gift.friend.id.eq(friendId)
                )
                .orderBy(gift.giftDate.desc())
                .fetch();
    }

    @Override
    public List<Gift> findAllWithFriendByMemberIdAndGiftType(Long memberId, GiftType giftType) {
        return queryFactory
                .selectFrom(gift)
                .join(gift.friend, friend).fetchJoin()
                .where(
                        gift.member.id.eq(memberId),
                        gift.giftType.eq(giftType)
                )
                .orderBy(gift.giftDate.desc())
                .fetch();
    }

    @Override
    public List<Gift> findAllWithFriendByMemberIdAndDirection(Long memberId, GiftDirection direction) {
        return queryFactory
                .selectFrom(gift)
                .join(gift.friend, friend).fetchJoin()
                .where(
                        gift.member.id.eq(memberId),
                        gift.direction.eq(direction)
                )
                .orderBy(gift.giftDate.desc())
                .fetch();
    }

    @Override
    public List<Gift> findAllWithFriendByMemberIdAndDateRange(Long memberId, LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .selectFrom(gift)
                .join(gift.friend, friend).fetchJoin()
                .where(
                        gift.member.id.eq(memberId),
                        gift.giftDate.between(startDate, endDate)
                )
                .orderBy(gift.giftDate.desc())
                .fetch();
    }

    @Override
    public Optional<Gift> findByIdAndMemberIdWithFriend(Long id, Long memberId) {
        Gift result = queryFactory
                .selectFrom(gift)
                .join(gift.friend, friend).fetchJoin()
                .where(
                        gift.id.eq(id),
                        gift.member.id.eq(memberId)
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
