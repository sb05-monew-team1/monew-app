package com.codeit.monew.notification.repository;

import java.time.Instant;
import java.util.List;

import com.codeit.monew.notification.dto.NotificationDto;

public interface NotificationQueryRepository {
	List<NotificationDto> search(
		String cursor,
		Instant after,
		int limit,
		String monewRequestUserId
	);

	long countUnConfirmed(String monewRequestUserId);
}
