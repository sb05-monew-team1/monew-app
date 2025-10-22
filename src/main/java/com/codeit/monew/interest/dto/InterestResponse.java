package com.codeit.monew.interest.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestKeyword;

public record InterestResponse(
	UUID id,
	String name,
	List<String> keywords,
	Long subscriberCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static InterestResponse from(Interest interest) {
		return new InterestResponse(
			interest.getId(),
			interest.getName(),
			interest.getKeywords().stream()
				.map(InterestKeyword::getKeyword)
				.collect(Collectors.toList()),
			interest.getSubscriberCount(),
			interest.getCreatedAt(),
			interest.getUpdatedAt()
		);
	}
}
