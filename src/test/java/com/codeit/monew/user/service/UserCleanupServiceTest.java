package com.codeit.monew.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codeit.monew.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserCleanupServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserCleanupService userCleanupService;

	@Test
	void cleanupDeletedUsersDeletesUsersBeforeCutoff() {
		ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);

		userCleanupService.cleanupDeletedUsers();

		verify(userRepository).deleteByDeletedAtBefore(captor.capture());
		Instant cutoff = captor.getValue();
		Instant expected = Instant.now().minus(24, ChronoUnit.HOURS);
		assertThat(Duration.between(cutoff, expected).abs()).isLessThan(Duration.ofSeconds(2));
	}

	@Test
	void cleanupDeletedUsersLogsOnError() {
		doThrow(new IllegalStateException("boom")).when(userRepository).deleteByDeletedAtBefore(any());

		userCleanupService.cleanupDeletedUsers();

		verify(userRepository).deleteByDeletedAtBefore(any());
	}
}
