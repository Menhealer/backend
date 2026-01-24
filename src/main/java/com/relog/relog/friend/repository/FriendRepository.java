package com.relog.relog.friend.repository;

import com.relog.relog.friend.entity.Friend;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long>, FriendRepositoryCustom {

    List<Friend> findAllByMemberId(Long memberId);

    Optional<Friend> findByIdAndMemberId(Long id, Long memberId);

    @Query("SELECT f FROM Friend f LEFT JOIN FETCH f.friendGroup WHERE f.id = :id AND f.member.id = :memberId")
    Optional<Friend> findByIdAndMemberIdWithGroup(@Param("id") Long id, @Param("memberId") Long memberId);

    boolean existsByMemberIdAndName(Long memberId, String name);
}
