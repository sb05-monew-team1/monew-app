package com.codeit.monew.notification.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.management.NotificationListener;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.codeit.monew.common.base.BaseUpdatableDomain;
import com.codeit.monew.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter @Setter
@EntityListeners(NotificationListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseUpdatableDomain {
	// user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
	// confirmed boolean NOT NULL DEFAULT false,
	// content varchar(500) NOT NULL,
	// resource_type varchar(20) NOT NULL,
	// resource_id uuid NOT NULL,

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private boolean confirmed;

	@Column(nullable = false, length = 500)
	private String content;

	@Column(name = "resource_type", nullable = false, length = 20)
	private String resourceType;

	@Column(name = "resource_id", nullable = false)
	private UUID resourceId;
}
