package com.codeit.monew.notification.service;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.codeit.monew.notification.domain.Notification;
import com.codeit.monew.notification.dto.NotificationCreateRequest;

@SpringBootTest
public class NotificationServiceTest {
	@Autowired
	private NotificationService notificationService;

	@Test
	@DisplayName("관심사 알림 생성 테스트")
	public void createInterestNotificationTest() {
		NotificationCreateRequest request = new NotificationCreateRequest(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			      "",
			      "interests",
			UUID.fromString("aaaa1111-0000-0000-0000-000000000001")
		);

		Notification createNoti = notificationService.create(request);
		System.out.println(createNoti);

		assert createNoti != null;
	}

	@Test
	@DisplayName("댓글 좋아요 알림 생성 테스트")
	public void createCommentNotificationTest() {
		NotificationCreateRequest request = new NotificationCreateRequest(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"",
			"comments",
			UUID.fromString("40000000-0000-0000-0000-000000000001")
		);

		Notification createNoti = notificationService.create(request);
		System.out.println(createNoti);

		assert createNoti != null;
	}
}
