package com.codeit.monew.notification.controller;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.notification.dto.NotificationDto;
import com.codeit.monew.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

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
		@RequestParam(name = "Monew-Request-User-ID") String monewRequestUserId
	) {
		return notificationService.getNotifications(
			cursor,
			after,
			limit,
			monewRequestUserId
		);
	}
}
