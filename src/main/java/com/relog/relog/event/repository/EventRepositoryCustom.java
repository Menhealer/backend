package com.relog.relog.event.repository;

import com.relog.relog.event.entity.Event;
import java.time.LocalDate;
import java.util.List;

public interface EventRepositoryCustom {

    List<Event> findAllWithFriendByMemberIdAndDateRange(Long memberId, LocalDate startDate, LocalDate endDate);

    List<Event> findAllWithFriendByMemberIdAndFriendId(Long memberId, Long friendId);

    List<Event> findAllWithFriendByMemberIdAndDate(Long memberId, LocalDate date);
}
