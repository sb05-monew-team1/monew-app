package com.codeit.monew.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
	UUID id,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	UUID userId,
	String content,
	String resourceType,
	UUID resourceId
) {
}
