package com.codeit.monew.notification.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.notification.dto.NotificationDto;
import com.codeit.monew.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public CursorPageResponse<NotificationDto> getNotifications(
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) Instant after,
		@RequestParam int limit,
		@RequestHeader("Monew-Request-User-ID") String monewRequestUserId
	) {
		log.info("GET api/notifications");

		return notificationService.getNotifications(
			cursor,
			after,
			limit,
			monewRequestUserId
		);
	}

	@PatchMapping
	public ResponseEntity<Void> checkAllNotifications(
		@RequestHeader("Monew-Request-User-ID") String monewRequestUserId) {
		log.info("PATCH /api/notifications");

		notificationService.checkAllNotifications(monewRequestUserId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{notificationId}")
	public ResponseEntity<Void> checkNotification(
		@PathVariable String notificationId,
		@RequestHeader("Monew-Request-User-ID") String monewRequestUserId
	) {
		log.info("PATCH /api/notifications/{}", notificationId);
		notificationService.checkNotification(notificationId, monewRequestUserId);
		return ResponseEntity.ok().build();
	}
}
