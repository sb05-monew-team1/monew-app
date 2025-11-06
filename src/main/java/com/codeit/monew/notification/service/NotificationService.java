package com.codeit.monew.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.notification.domain.Notification;
import com.codeit.monew.notification.dto.NotificationCreateRequest;
import com.codeit.monew.notification.dto.NotificationDto;
import com.codeit.monew.notification.exception.NotificationNotFoundException;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
	private static final String METRIC_NOTIFICATION_CREATE_SUCCESS = "notification.create.success";
	private static final String METRIC_NOTIFICATION_UNCONFIRMED_SAMPLE = "notification.unconfirmed.count";
	private static final String METRIC_NOTIFICATION_CONFIRM_SUCCESS = "notification.confirm.success";
	private static final String METRIC_NOTIFICATION_CONFIRM_FORBIDDEN = "notification.confirm.forbidden";
	private static final String METRIC_NOTIFICATION_CONFIRM_NOT_FOUND = "notification.confirm.not_found";

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final PageResponseMapper pageResponseMapper;
	private final MeterRegistry meterRegistry;

	public Notification create(NotificationCreateRequest request) {
		log.debug("알림 등록 시작 - userId: {}, resourceType: {}", request.userId(), request.resourceType());

		User user = userRepository.findById(request.userId())
			.orElseThrow(
				() -> new UserNotFoundException().addDetail("NotificationCreateRequest.userId", request.userId()));

		Notification notification = Notification.builder()
			.user(user)
			.confirmed(false)
			.content(request.content())
			.resourceType(request.resourceType())
			.resourceId(request.resourceId())
			.build();

		notificationRepository.save(notification);
		log.info("알림 등록 - id: {}, resourceType: {}, content: {}", notification.getId(), notification.getResourceType(),
			notification.getContent());
		meterRegistry.counter(METRIC_NOTIFICATION_CREATE_SUCCESS).increment();

		return notification;
	}

	public CursorPageResponse<NotificationDto> getNotifications(
		String cursor,
		Instant after,
		int limit,
		String monewRequestUserId
	) {
		log.debug("알림 목록 조회 시작 - cursor: {}, after: {}, userId: {}",
			cursor, after, monewRequestUserId);
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
			log.debug("다음 페이지 커서 생성 - nextCursor: {}, nextAfter: {}", nextCursor, nextAfter);
		}

		long totalElements = notificationRepository.countUnConfirmed(monewRequestUserId);
		meterRegistry.summary(METRIC_NOTIFICATION_UNCONFIRMED_SAMPLE).record(totalElements);

		return pageResponseMapper.toCursorPageResponse(slice, nextCursor, nextAfter, totalElements);
	}

	public void checkAllNotifications(String monewRequestUserId) {
		try {
			log.debug("모든 알림 확인 시작: userId: {}", monewRequestUserId);
			UUID userId = UUID.fromString(monewRequestUserId);

			if (!userRepository.existsById(userId)) {
				meterRegistry.counter(METRIC_NOTIFICATION_CONFIRM_FORBIDDEN, "scope", "all").increment();
				throw new UserNotFoundException();
			}

			List<Notification> notifications = notificationRepository.findByUserIdAndConfirmedFalse((userId));
			for (Notification notification : notifications) {
				notification.setConfirmed(true);
			}

			notificationRepository.saveAll(notifications);
			log.info("모든 알림 확인 완료: userId: {}", userId);
			if (!notifications.isEmpty()) {
				meterRegistry.counter(METRIC_NOTIFICATION_CONFIRM_SUCCESS, "scope", "all")
					.increment(notifications.size());
			}
		} catch (IllegalArgumentException e) {
			log.error("UUID 변환 실패: {}", monewRequestUserId, e);
			throw new IllegalArgumentException(e);
		}
	}

	public void checkNotification(String id, String monewRequestUserId) {
		try {
			log.debug("알림 확인 시작: notificationId: {}, userId: {}", id, monewRequestUserId);
			UUID notificationId = UUID.fromString(id);
			UUID userId = UUID.fromString(monewRequestUserId);

			if (!userRepository.existsById(userId)) {
				meterRegistry.counter(METRIC_NOTIFICATION_CONFIRM_FORBIDDEN, "scope", "single").increment();
				throw new UserNotFoundException();
			}

			if (!notificationRepository.existsById(notificationId)) {
				meterRegistry.counter(METRIC_NOTIFICATION_CONFIRM_NOT_FOUND).increment();
				throw new NotificationNotFoundException();
			}

			Notification notification = notificationRepository.findByIdAndUserIdAndConfirmedFalse(notificationId,
					userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
			notification.setConfirmed(true);

			notificationRepository.save(notification);
			log.info("알림 확인 완료: notificationId: {}, userId: {}", notificationId, userId);
			meterRegistry.counter(METRIC_NOTIFICATION_CONFIRM_SUCCESS, "scope", "single").increment();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
