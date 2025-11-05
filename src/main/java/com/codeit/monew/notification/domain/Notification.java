package com.codeit.monew.notification.domain;

import java.util.UUID;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.monew.common.base.BaseUpdatableDomain;
import com.codeit.monew.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notifications")
@SuperBuilder
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseUpdatableDomain {
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
