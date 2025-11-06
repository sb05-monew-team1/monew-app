package com.codeit.monew.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.notification.dto.NotificationDto;
import com.codeit.monew.notification.service.NotificationService;

class NotificationControllerTest {

	private final NotificationService notificationService = mock(NotificationService.class);
	private final NotificationController controller = new NotificationController(notificationService);

	@Test
	void getNotificationsDelegatesToService() {
		CursorPageResponse<NotificationDto> response = CursorPageResponse.<NotificationDto>builder().build();
		when(notificationService.getNotifications("cursor", Instant.EPOCH, 20, "user"))
			.thenReturn(response);

		CursorPageResponse<NotificationDto> result =
			controller.getNotifications("cursor", Instant.EPOCH, 20, "user");

		assertThat(result).isSameAs(response);
		verify(notificationService).getNotifications("cursor", Instant.EPOCH, 20, "user");
	}

	@Test
	void checkAllNotificationsReturnsOk() {
		assertThat(controller.checkAllNotifications("user").getStatusCode().is2xxSuccessful()).isTrue();
		verify(notificationService).checkAllNotifications("user");
	}

	@Test
	void checkNotificationReturnsOk() {
		assertThat(controller.checkNotification("id", "user").getStatusCode().is2xxSuccessful()).isTrue();
		verify(notificationService).checkNotification("id", "user");
	}
}
