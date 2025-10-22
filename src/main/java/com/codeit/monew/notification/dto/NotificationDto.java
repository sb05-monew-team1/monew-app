package com.codeit.monew.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
	UUID id,
	Instant createdAt,
	Instant updatedAt,
	UUID userId,
	String content,
	String resourceType,
	UUID resourceId
) {
}
