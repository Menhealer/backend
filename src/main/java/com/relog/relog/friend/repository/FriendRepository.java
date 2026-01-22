package com.relog.relog.friend.repository;

import com.relog.relog.friend.entity.Friend;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findAllByMemberId(Long memberId);

    Optional<Friend> findByIdAndMemberId(Long id, Long memberId);

    List<Friend> findAllByMemberIdAndFriendGroupId(Long memberId, Long groupId);

    @Query("SELECT f FROM Friend f WHERE f.member.id = :memberId AND " +
           "MONTH(f.birthday) = :month AND DAY(f.birthday) = :day")
    List<Friend> findByMemberIdAndBirthdayMonthAndDay(
            @Param("memberId") Long memberId,
            @Param("month") int month,
            @Param("day") int day);

    @Query("SELECT f FROM Friend f WHERE f.member.id = :memberId AND f.birthday BETWEEN :startDate AND :endDate")
    List<Friend> findByMemberIdAndBirthdayBetween(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
