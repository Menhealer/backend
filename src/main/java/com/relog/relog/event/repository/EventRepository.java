package com.relog.relog.event.repository;

import com.relog.relog.event.entity.Event;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByMemberId(Long memberId);

    Optional<Event> findByIdAndMemberId(Long id, Long memberId);

    List<Event> findAllByMemberIdAndFriendId(Long memberId, Long friendId);

    List<Event> findAllByMemberIdAndEventDate(Long memberId, LocalDate eventDate);

    @Query("SELECT e FROM Event e WHERE e.member.id = :memberId AND " +
           "e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate ASC")
    List<Event> findAllByMemberIdAndEventDateBetween(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Event e WHERE e.member.id = :memberId AND " +
           "YEAR(e.eventDate) = :year AND MONTH(e.eventDate) = :month ORDER BY e.eventDate ASC")
    List<Event> findAllByMemberIdAndYearAndMonth(
            @Param("memberId") Long memberId,
            @Param("year") int year,
            @Param("month") int month);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.member.id = :memberId AND e.friend.id = :friendId")
    long countByMemberIdAndFriendId(
            @Param("memberId") Long memberId,
            @Param("friendId") Long friendId);

    @Query("SELECT e.friend.id, COUNT(e) FROM Event e WHERE e.member.id = :memberId " +
           "AND e.eventDate BETWEEN :startDate AND :endDate GROUP BY e.friend.id ORDER BY COUNT(e) DESC")
    List<Object[]> findFriendEventCountByMemberIdAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
