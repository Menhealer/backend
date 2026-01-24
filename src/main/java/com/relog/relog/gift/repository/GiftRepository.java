package com.relog.relog.gift.repository;

import com.relog.relog.gift.entity.Gift;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long>, GiftRepositoryCustom {

    Optional<Gift> findByIdAndMemberId(Long id, Long memberId);

    @Query("SELECT g FROM Gift g JOIN FETCH g.friend WHERE g.id = :id AND g.member.id = :memberId")
    Optional<Gift> findByIdAndMemberIdWithFriend(@Param("id") Long id, @Param("memberId") Long memberId);
}
