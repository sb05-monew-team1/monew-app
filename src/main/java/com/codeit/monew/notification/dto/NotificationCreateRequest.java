package com.codeit.monew.notification.dto;

import java.util.UUID;

public record NotificationCreateRequest(
	UUID userId,
	String content,
	String resourceType,
	UUID resourceId
) {
}
