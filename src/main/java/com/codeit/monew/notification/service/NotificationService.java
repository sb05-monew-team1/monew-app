package com.codeit.monew.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.notification.domain.Notification;
import com.codeit.monew.notification.dto.NotificationCreateRequest;
import com.codeit.monew.notification.dto.NotificationDto;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;

	public Notification create(NotificationCreateRequest request) {
		User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new RuntimeException("User not found"));

		Notification notification = Notification.builder()
			.user(user)
			.confirmed(false)
			.content(request.content())
			.resourceType(request.resourceType())
			.resourceId(request.resourceId())
			.build();

		notificationRepository.save(notification);
		return notification;
	}

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

	public void checkAllNotifications(String monewRequestUserId) {
		try {
			UUID userId = UUID.fromString(monewRequestUserId);

			if (!userRepository.existsById(userId)) {
				throw new RuntimeException("User not found");
			}

			List<Notification> notifications = notificationRepository.findByUserIdAndConfirmedFalse((userId));
			for(Notification notification : notifications) {
				notification.setConfirmed(true);
			}

			notificationRepository.saveAll(notifications);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void checkNotification(String id, String monewRequestUserId) {
		try {
			UUID notificationId =  UUID.fromString(id);
			UUID userId = UUID.fromString(monewRequestUserId);

			if (!userRepository.existsById(userId)) {
				throw new RuntimeException("User not found");
			}

			Notification notification = notificationRepository.findByIdAndUserIdAndConfirmedFalse(notificationId, userId);
			notification.setConfirmed(true);

			notificationRepository.save(notification);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
