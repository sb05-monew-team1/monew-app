package com.codeit.monew.notification.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.codeit.monew.notification.domain.Notification;
import com.codeit.monew.notification.dto.NotificationCreateRequest;
import com.codeit.monew.notification.repository.NotificationRepository;

@SpringBootTest
public class NotificationServiceTest {
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private NotificationRepository notificationRepository;

	@Nested
	class CreateNotification {
			@Test
			@DisplayName("관심사 알림 생성 테스트")
			public void createInterestNotificationTest () {
			NotificationCreateRequest request = new NotificationCreateRequest(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"",
				"interests",
				UUID.fromString("aaaa1111-0000-0000-0000-000000000001")
			);

			Notification createNoti = notificationService.create(request);
			System.out.println("알림 : " + createNoti);

			assert createNoti != null;
		}

			@Test
			@DisplayName("댓글 좋아요 알림 생성 테스트")
			public void createCommentNotificationTest () {
			NotificationCreateRequest request = new NotificationCreateRequest(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"",
				"comments",
				UUID.fromString("40000000-0000-0000-0000-000000000001")
			);

			Notification createNoti = notificationService.create(request);
			System.out.println("알림 : " + createNoti);

			assert createNoti != null;
		}
	}

	@Nested
	class CheckAllNotifications{
			@Test
			@DisplayName("모든 알림 확인 테스트 - 성공")
			public void checkNotifisSuccessTest () {
			String userIdStr = "11111111-1111-1111-1111-111111111111";
			notificationService.checkAllNotifications(userIdStr);

			UUID userId = UUID.fromString(userIdStr);

			List<Notification> list = notificationRepository.findByUserIdAndConfirmedFalse(userId);
			assertTrue(list.isEmpty(), "모든 알림은 확인했으면 confirmed가 true로 변경되어야 한다.");

		}

		@Test
		@DisplayName("모든 알림 확인 테스트 - 실패(해당 유저가 존재하지 않음)")
		public void checkNotifisFailTest () {
			String userIdStr = "11111111-1111-1111-1111-111111111113";

			assertThrows(RuntimeException.class, () -> {
				notificationService.checkAllNotifications(userIdStr);
			});
		}
	}

	@Nested
	class CheckNotification {
		@Test
		@DisplayName("알림 확인 테스트 - 성공")
		public void checkNotificationSuccessTest() {
			String notificationIdStr = "50000000-0000-0000-0000-000000000001";
			String userIdStr = "11111111-1111-1111-1111-111111111111";
			notificationService.checkNotification(notificationIdStr, userIdStr);

			UUID notificationId = UUID.fromString(notificationIdStr);
			UUID userId = UUID.fromString(userIdStr);

			Notification notification = notificationRepository.findByIdAndUserIdAndConfirmedTrue(notificationId, userId);
			System.out.println("알림 : " +  notification.toString());

			assertTrue(notification.isConfirmed() == true);
		}

		@Test
		@DisplayName("알림 확인 테스트 - 실패(존재하지 않는 유저)")
		public void checkNotifiUserFailTest() {
			String notificationIdStr = "50000000-0000-0000-0000-000000000001";
			String userIdStr = "11111111-1111-1111-1111-111111111131";

			assertThrows(RuntimeException.class, () -> {
				notificationService.checkNotification(notificationIdStr, userIdStr);
			});
		}

		@Test
		@DisplayName("알림 확인 테스트 - 실패(존재하지 않는 알림)")
		public void checkNotifiFailTest() {
			String notificationIdStr = "50000000-0000-0000-0000-000011000001";
			String userIdStr = "11111111-1111-1111-1111-111111111111";

			assertThrows(RuntimeException.class, () -> {
				notificationService.checkNotification(notificationIdStr, userIdStr);
			});
		}
	}
}
