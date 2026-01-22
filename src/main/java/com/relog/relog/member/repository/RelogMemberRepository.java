package com.relog.relog.member.repository;

import com.relog.relog.member.entity.RelogMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelogMemberRepository extends JpaRepository<RelogMember, Long> {

    Optional<RelogMember> findByEmail(String email);

    boolean existsByEmail(String email);
}
