package com.codeit.monew.interest.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.monew.interest.domain.InterestSubscription;

public interface InterestSubscriptionRepository extends JpaRepository<InterestSubscription, UUID> {

}
