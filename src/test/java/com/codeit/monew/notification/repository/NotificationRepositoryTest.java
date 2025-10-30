package com.codeit.monew.notification.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import com.codeit.monew.notification.dto.NotificationDto;

@ActiveProfiles("test")
@SpringBootTest
public class NotificationRepositoryTest {

	@Autowired
	private NotificationRepository notificationRepository;

	@Test
	void searchNotifications() {
		String userId = "11111111-1111-1111-1111-111111111111";
		Slice<NotificationDto> slice = notificationRepository.search(null, null, 2, userId);

		assertThat(slice.hasNext()).isTrue();
		assertThat(slice.getContent()).hasSize(2);
		assertThat(slice.getContent())
			.extracting(NotificationDto::id)
			.containsExactly(
				UUID.fromString("50000000-0000-0000-0000-000000000004"),
				UUID.fromString("50000000-0000-0000-0000-000000000003")
			);

		NotificationDto first = slice.getContent().get(0);
		NotificationDto second = slice.getContent().get(1);

		assertThat(first.content()).isEqualTo("알림 테스트용 데이터3");
		assertThat(first.resourceId()).isEqualTo(UUID.fromString("40000000-0000-0000-0000-000000000003"));
		assertThat(first.resourceType()).isEqualTo("comments");

		assertThat(second.content()).isEqualTo("알림 테스트용 데이터2");
		assertThat(second.resourceId()).isEqualTo(UUID.fromString("40000000-0000-0000-0000-000000000002"));
		assertThat(second.resourceType()).isEqualTo("comments");
	}

	@Test
	void searchNotificationsNextPage() {
		String userId = "11111111-1111-1111-1111-111111111111";
		Slice<NotificationDto> firstSlice = notificationRepository.search(null, null, 2, userId);

		assertThat(firstSlice.hasNext()).isTrue();
		assertThat(firstSlice.getContent()).hasSize(2);
		assertThat(firstSlice.getContent())
			.extracting(NotificationDto::id)
			.containsExactly(
				UUID.fromString("50000000-0000-0000-0000-000000000004"),
				UUID.fromString("50000000-0000-0000-0000-000000000003")
			);
		assertThat(firstSlice.getContent())
			.extracting(NotificationDto::content)
			.containsExactly("알림 테스트용 데이터3", "알림 테스트용 데이터2");

		NotificationDto lastNotification = firstSlice.getContent().get(1);
		String cursor = String.valueOf(lastNotification.createdAt());
		Instant after = lastNotification.createdAt();

		System.out.println("메서드 호출 전 cursor : " + cursor + ", after : " + after + ", userId: " + userId);
		Slice<NotificationDto> secondSlice = notificationRepository.search(cursor, after, 2, userId);

		assertThat(secondSlice.hasNext()).isTrue();
		assertThat(secondSlice.getContent())
			.extracting(NotificationDto::id)
			.containsExactly(
				UUID.fromString("50000000-0000-0000-0000-000000000005"),
				UUID.fromString("50000000-0000-0000-0000-000000000001")
			);
		assertThat(secondSlice.getContent())
			.extracting(NotificationDto::content)
			.containsExactly("알림 테스트용 데이터4", "알림 테스트용 데이터1");
	}
}
