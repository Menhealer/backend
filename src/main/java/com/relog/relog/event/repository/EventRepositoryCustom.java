package com.relog.relog.event.repository;

import com.relog.relog.event.entity.Event;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepositoryCustom {

    List<Event> findAllWithFriendByMemberIdAndDateRange(Long memberId, LocalDate startDate, LocalDate endDate);

    List<Event> findAllWithFriendByMemberIdAndFriendId(Long memberId, Long friendId);

    Optional<Event> findByIdAndMemberIdWithFriend(Long id, Long memberId);
}
