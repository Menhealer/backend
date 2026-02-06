package com.relog.relog.friend.repository;

import com.relog.relog.friend.entity.Friend;
import java.util.List;

public interface FriendRepositoryCustom {

    List<Friend> findAllByMemberIdOrderByName(Long memberId);
}
