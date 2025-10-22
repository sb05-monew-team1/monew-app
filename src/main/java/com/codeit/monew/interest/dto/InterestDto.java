package com.codeit.monew.interest.dto;

import java.util.List;
import java.util.UUID;

public record InterestDto(
	UUID id,
	String name,
	List<String> keywords,
	Long subscriberCount,
	boolean subscribedByMe
) {
}
