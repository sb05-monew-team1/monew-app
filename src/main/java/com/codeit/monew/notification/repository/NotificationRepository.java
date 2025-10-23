package com.codeit.monew.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.monew.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationQueryRepository {
	List<Notification> findByUserIdAndConfirmedFalse(UUID userId);

	Notification findByIdAndUserIdAndConfirmedFalse(UUID notificationId, UUID userId);
}
