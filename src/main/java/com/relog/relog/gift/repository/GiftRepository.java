package com.relog.relog.gift.repository;

import com.relog.relog.gift.entity.Gift;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long>, GiftRepositoryCustom {

    Optional<Gift> findByIdAndMemberId(Long id, Long memberId);

    List<Gift> findAllByMemberId(Long memberId);
}
