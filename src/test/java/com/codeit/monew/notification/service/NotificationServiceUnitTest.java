package com.codeit.monew.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.notification.domain.Notification;
import com.codeit.monew.notification.dto.NotificationCreateRequest;
import com.codeit.monew.notification.dto.NotificationDto;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

	@Mock
	private NotificationRepository notificationRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private PageResponseMapper pageResponseMapper;
	@Mock
	private MeterRegistry meterRegistry;

	@InjectMocks
	private NotificationService notificationService;

	private final Map<String, Counter> counters = new HashMap<>();
	private final Map<String, DistributionSummary> summaries = new HashMap<>();

	@BeforeEach
	void setUpMeterRegistry() {
		counters.clear();
		summaries.clear();
		Mockito.lenient().when(meterRegistry.counter(anyString()))
			.thenAnswer(invocation -> counters.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(Counter.class)));
		Mockito.lenient().when(meterRegistry.counter(anyString(), any(String[].class)))
			.thenAnswer(invocation -> counters.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(Counter.class)));
		Mockito.lenient().when(meterRegistry.summary(anyString()))
			.thenAnswer(invocation -> summaries.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(DistributionSummary.class)));
		Mockito.lenient().when(meterRegistry.summary(anyString(), any(String[].class)))
			.thenAnswer(invocation -> summaries.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(DistributionSummary.class)));
	}

	@Test
	@DisplayName("알림 목록 조회 시 다음 커서를 계산한다")
	void getNotificationsBuildsCursor() {
		NotificationDto dto = new NotificationDto(
			UUID.randomUUID(),
			Instant.parse("2024-03-10T10:00:00Z"),
			Instant.parse("2024-03-10T10:01:00Z"),
			UUID.randomUUID(),
			"content",
			"type",
			UUID.randomUUID()
		);
		Slice<NotificationDto> slice = new SliceImpl<>(List.of(dto), Pageable.ofSize(1), true);

		given(notificationRepository.search(any(), any(), anyInt(), anyString())).willReturn(slice);
		given(notificationRepository.countUnConfirmed("user")).willReturn(3L);
		given(pageResponseMapper.toCursorPageResponse(any(), any(), any(), anyLong()))
			.willAnswer(invocation -> {
				assertThat(invocation.getArgument(1, String.class)).isEqualTo(dto.createdAt().toString());
				assertThat(invocation.getArgument(2, String.class)).isEqualTo(dto.createdAt().toString());
				Number totalElements = invocation.getArgument(3, Number.class);
				return CursorPageResponse.<NotificationDto>builder()
					.content(slice.getContent())
					.nextCursor(invocation.getArgument(1, String.class))
					.nextAfter(invocation.getArgument(2, String.class))
					.size(slice.getNumberOfElements())
					.totalElements(totalElements.longValue())
					.hasNext(slice.hasNext())
					.build();
			});

		CursorPageResponse<NotificationDto> response = notificationService.getNotifications(null, null, 1, "user");

		assertThat(response.nextCursor()).isEqualTo(dto.createdAt().toString());
		assertThat(response.nextAfter()).isEqualTo(dto.createdAt().toString());
		assertThat(response.totalElements()).isEqualTo(3L);

		DistributionSummary summary = summaries.get("notification.unconfirmed.count");
		assertThat(summary).isNotNull();
		verify(summary).record(3L);
	}

	@Test
	@DisplayName("알림 생성 시 사용자 검증 후 저장한다")
	void createNotificationPersistsEntity() {
		UUID userId = UUID.randomUUID();
		NotificationCreateRequest request = new NotificationCreateRequest(
			userId,
			"message",
			"type",
			UUID.randomUUID()
		);

		User user = User.builder()
			.id(userId)
			.email("tester@example.com")
			.nickname("tester")
			.password("secret")
			.build();

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(notificationRepository.save(any(Notification.class))).willAnswer(invocation -> invocation.getArgument(0));

		Notification notification = notificationService.create(request);

		ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
		verify(notificationRepository).save(captor.capture());

		assertThat(notification.getUser()).isEqualTo(user);
		assertThat(notification.getContent()).isEqualTo("message");
		assertThat(notification.getResourceType()).isEqualTo("type");
		assertThat(captor.getValue().isConfirmed()).isFalse();

		Counter counter = counters.get("notification.create.success");
		assertThat(counter).isNotNull();
		verify(counter).increment();
	}

	@Test
	@DisplayName("모든 알림 확인 시 성공 지표를 증가시킨다")
	void checkAllNotificationsUpdatesMetrics() {
		UUID userId = UUID.randomUUID();
		User user = User.builder()
			.id(userId)
			.email("user@example.com")
			.nickname("user")
			.password("secret")
			.build();

		Notification first = Notification.builder()
			.id(UUID.randomUUID())
			.user(user)
			.confirmed(false)
			.content("content1")
			.resourceType("type")
			.resourceId(UUID.randomUUID())
			.build();
		Notification second = Notification.builder()
			.id(UUID.randomUUID())
			.user(user)
			.confirmed(false)
			.content("content2")
			.resourceType("type")
			.resourceId(UUID.randomUUID())
			.build();

		given(userRepository.existsById(userId)).willReturn(true);
		given(notificationRepository.findByUserIdAndConfirmedFalse(userId)).willReturn(List.of(first, second));
		given(notificationRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

		notificationService.checkAllNotifications(userId.toString());

		assertThat(first.isConfirmed()).isTrue();
		assertThat(second.isConfirmed()).isTrue();
		Counter counter = counters.get("notification.confirm.success");
		assertThat(counter).isNotNull();
		verify(counter).increment(2);
	}

	@Test
	@DisplayName("단일 알림 확인 시 성공 지표를 증가시킨다")
	void checkNotificationUpdatesMetrics() {
		UUID notificationId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		User user = User.builder()
			.id(userId)
			.email("user@example.com")
			.nickname("user")
			.password("secret")
			.build();
		Notification notification = Notification.builder()
			.id(notificationId)
			.user(user)
			.confirmed(false)
			.content("content")
			.resourceType("type")
			.resourceId(UUID.randomUUID())
			.build();

		given(userRepository.existsById(userId)).willReturn(true);
		given(notificationRepository.existsById(notificationId)).willReturn(true);
		given(notificationRepository.findByIdAndUserIdAndConfirmedFalse(notificationId, userId))
			.willReturn(Optional.of(notification));
		given(notificationRepository.save(notification)).willReturn(notification);

		notificationService.checkNotification(notificationId.toString(), userId.toString());

		assertThat(notification.isConfirmed()).isTrue();
		Counter counter = counters.get("notification.confirm.success");
		assertThat(counter).isNotNull();
		verify(counter).increment();
	}
}
