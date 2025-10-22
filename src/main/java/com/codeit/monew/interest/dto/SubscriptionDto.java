package com.codeit.monew.interest.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubscriptionDto(
	UUID id,
	UUID interestId,
	String interestName,
	List<String> interestKeywords,
	Long interestSubscriberCount,
	Instant createdAt
) {
}
