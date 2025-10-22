package com.codeit.monew.notification.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.notification.domain.Notification;
import com.codeit.monew.notification.dto.NotificationDto;
import com.codeit.monew.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;

	public CursorPageResponse<NotificationDto> getNotifications(
		String cursor,
		Instant after,
		int limit,
		String monewRequestUserId
	) {
		List<NotificationDto> rowPlusOne = notificationRepository.search(
			cursor,
			after,
			limit,
			monewRequestUserId
		);
		boolean hasNext = rowPlusOne.size() > limit;
		List<NotificationDto> content = hasNext ? rowPlusOne.subList(0, limit) : rowPlusOne;
		String nextCursor = hasNext ? content.get(content.size() - 1).createdAt().toString() : null;
		String nextAfter = hasNext ? content.get(content.size() - 1).createdAt().toString() : null;
		long totalElements = notificationRepository.countUnConfirmed(monewRequestUserId);

		return new CursorPageResponse<>(
			content,
			nextCursor,
			nextAfter,
			limit,
			totalElements,
			hasNext
		);
	}

}
