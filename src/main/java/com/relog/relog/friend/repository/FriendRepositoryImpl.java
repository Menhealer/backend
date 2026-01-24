package com.relog.relog.friend.repository;

import static com.relog.relog.friend.entity.QFriend.friend;
import static com.relog.relog.friendgroup.entity.QFriendGroup.friendGroup;

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
    public List<Friend> findAllWithGroupByMemberId(Long memberId) {
        return queryFactory
                .selectFrom(friend)
                .leftJoin(friend.friendGroup, friendGroup).fetchJoin()
                .where(friend.member.id.eq(memberId))
                .orderBy(friend.name.asc())
                .fetch();
    }

    @Override
    public List<Friend> findAllWithGroupByMemberIdAndGroupId(Long memberId, Long groupId) {
        return queryFactory
                .selectFrom(friend)
                .leftJoin(friend.friendGroup, friendGroup).fetchJoin()
                .where(
                        friend.member.id.eq(memberId),
                        friend.friendGroup.id.eq(groupId)
                )
                .orderBy(friend.name.asc())
                .fetch();
    }
}
