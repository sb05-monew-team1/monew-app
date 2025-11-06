package com.codeit.monew.interest.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestSubscription;
import com.codeit.monew.user.domain.User;

public interface InterestSubscriptionRepository
	extends JpaRepository<InterestSubscription, UUID>, InterestSubscriptionQueryRepository {
	@Query("SELECT s.interest.id FROM InterestSubscription s WHERE s.user.id = :userId AND s.interest.id IN :interestIds")
	Set<UUID> findInterestIdsByUserIdAndInterestIdsIn(@Param("userId") UUID userId,
		@Param("interestIds") List<UUID> interestIds);

	Optional<InterestSubscription> findByUserAndInterest(User user, Interest interest);

	boolean existsByUserAndInterest(User user, Interest interest);
}
