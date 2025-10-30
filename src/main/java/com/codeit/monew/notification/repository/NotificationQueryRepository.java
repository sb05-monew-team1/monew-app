package com.codeit.monew.notification.repository;

import java.time.Instant;

import org.springframework.data.domain.Slice;

import com.codeit.monew.notification.dto.NotificationDto;

public interface NotificationQueryRepository {
	Slice<NotificationDto> search(
		String cursor,
		Instant after,
		int limit,
		String monewRequestUserId
	);

	long countUnConfirmed(String monewRequestUserId);
}
