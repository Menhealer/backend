package com.relog.relog.friendgroup.repository;

import com.relog.relog.friendgroup.entity.FriendGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendGroupRepository extends JpaRepository<FriendGroup, Long> {

    List<FriendGroup> findAllByMemberId(Long memberId);

    Optional<FriendGroup> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByNameAndMemberId(String name, Long memberId);
}
