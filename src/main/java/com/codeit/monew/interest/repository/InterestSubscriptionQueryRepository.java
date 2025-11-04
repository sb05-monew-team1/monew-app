package com.codeit.monew.interest.repository;

import java.util.List;
import java.util.UUID;

import com.codeit.monew.interest.dto.SubscriptionDto;

public interface InterestSubscriptionQueryRepository {
	List<SubscriptionDto> searchSubsCription(UUID userId);
}
