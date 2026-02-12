package com.relog.relog.member.repository;

import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.entity.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RelogMemberRepository extends JpaRepository<RelogMember, Long> {

    Optional<RelogMember> findByProviderAndProviderId(SocialProvider provider, String providerId);

    @Query(value = "SELECT * FROM relog_member WHERE provider = :provider AND provider_id = :providerId", nativeQuery = true)
    Optional<RelogMember> findByProviderAndProviderIdWithDeleted(@Param("provider") String provider, @Param("providerId") String providerId);
}