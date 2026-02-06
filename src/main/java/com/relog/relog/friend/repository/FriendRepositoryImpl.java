package com.relog.relog.friend.repository;

import static com.relog.relog.friend.entity.QFriend.friend;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.relog.relog.friend.entity.Friend;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Friend> findAllByMemberIdOrderByName(Long memberId) {
        return queryFactory
                .selectFrom(friend)
                .where(friend.member.id.eq(memberId))
                .orderBy(friend.name.asc())
                .fetch();
    }
}
