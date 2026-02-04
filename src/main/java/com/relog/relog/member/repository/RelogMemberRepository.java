package com.relog.relog.member.repository;

import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.entity.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelogMemberRepository extends JpaRepository<RelogMember, Long> {

    Optional<RelogMember> findByProviderAndProviderId(SocialProvider provider, String providerId);

    Optional<RelogMember> findByEmail(String email);
}
