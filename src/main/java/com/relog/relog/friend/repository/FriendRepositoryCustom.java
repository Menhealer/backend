package com.relog.relog.friend.repository;

import com.relog.relog.friend.entity.Friend;
import java.util.List;
import java.util.Optional;

public interface FriendRepositoryCustom {

    List<Friend> findAllWithGroupByMemberId(Long memberId);

    List<Friend> findAllWithGroupByMemberIdAndGroupId(Long memberId, Long groupId);

    Optional<Friend> findByIdAndMemberIdWithGroup(Long id, Long memberId);
}
