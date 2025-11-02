package com.codeit.monew.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.util.PageResponseMapper;
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
	private final PageResponseMapper pageResponseMapper;

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
		Slice<NotificationDto> slice = notificationRepository.search(
			cursor,
			after,
			limit,
			monewRequestUserId
		);

		String nextCursor = null;
		String nextAfter = null;

		if (slice.hasNext() && slice.getNumberOfElements() > 0) {
			NotificationDto last = slice.getContent().get(slice.getNumberOfElements() - 1);

			nextAfter = last.createdAt().toString();
			nextCursor = last.createdAt().toString();
		}

		long totalElements = notificationRepository.countUnConfirmed(monewRequestUserId);

		return pageResponseMapper.toCursorPageResponse(slice, nextCursor, nextAfter, totalElements);
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

			if(!notificationRepository.existsById(notificationId)) {
				throw new RuntimeException("Notification not found");
			}

			Notification notification = notificationRepository.findByIdAndUserIdAndConfirmedFalse(notificationId, userId)
				.orElseThrow(() -> new RuntimeException("Notification not found"));
			notification.setConfirmed(true);

			notificationRepository.save(notification);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
