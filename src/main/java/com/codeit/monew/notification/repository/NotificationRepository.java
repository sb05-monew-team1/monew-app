package com.codeit.monew.notification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.monew.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationQueryRepository {
	List<Notification> findByUserIdAndConfirmedFalse(UUID userId);

	Optional<Notification> findByIdAndUserIdAndConfirmedFalse(UUID notificationId, UUID userId);

	Optional<Notification> findByIdAndUserIdAndConfirmedTrue(UUID id, UUID userId);
}
