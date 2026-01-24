package com.relog.relog.event.repository;

import com.relog.relog.event.entity.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

    Optional<Event> findByIdAndMemberId(Long id, Long memberId);

    @Query("SELECT e FROM Event e JOIN FETCH e.friend WHERE e.id = :id AND e.member.id = :memberId")
    Optional<Event> findByIdAndMemberIdWithFriend(@Param("id") Long id, @Param("memberId") Long memberId);
}
