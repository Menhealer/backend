package com.relog.relog.event.repository;

import static com.relog.relog.event.entity.QEvent.event;
import static com.relog.relog.friend.entity.QFriend.friend;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.relog.relog.event.entity.Event;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> findAllWithFriendByMemberIdAndDateRange(Long memberId, LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .selectFrom(event)
                .join(event.friend, friend).fetchJoin()
                .where(
                        event.member.id.eq(memberId),
                        event.eventDate.between(startDate, endDate)
                )
                .orderBy(event.eventDate.asc())
                .fetch();
    }

    @Override
    public List<Event> findAllWithFriendByMemberIdAndFriendId(Long memberId, Long friendId) {
        return queryFactory
                .selectFrom(event)
                .join(event.friend, friend).fetchJoin()
                .where(
                        event.member.id.eq(memberId),
                        event.friend.id.eq(friendId)
                )
                .orderBy(event.eventDate.desc())
                .fetch();
    }

    @Override
    public List<Event> findAllWithFriendByMemberIdAndDate(Long memberId, LocalDate date) {
        return queryFactory
                .selectFrom(event)
                .join(event.friend, friend).fetchJoin()
                .where(
                        event.member.id.eq(memberId),
                        event.eventDate.eq(date)
                )
                .orderBy(event.eventDate.asc())
                .fetch();
    }
}
