package com.codeit.monew.interest.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.monew.interest.domain.InterestSubscription;

public interface InterestSubscriptionRepository extends JpaRepository<InterestSubscription, UUID> {
	@Query("SELECT s.interest.id FROM InterestSubscription s WHERE s.user.id = :userId AND s.interest.id IN :interestIds")
	Set<UUID> findInterestIdsByUserIdAndInterestIdsIn(@Param("userId") UUID userId,
		@Param("interestIds") List<UUID> interestIds);
}
