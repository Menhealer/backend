package com.relog.relog.event.repository;

import com.relog.relog.event.entity.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

    Optional<Event> findByIdAndMemberId(Long id, Long memberId);
}
