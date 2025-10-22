package com.codeit.monew.interest.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.codeit.monew.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "interest_subscriptions",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_interest_user",
		columnNames = {"interest_id", "user_id"}
	))
@AllArgsConstructor
@NoArgsConstructor
public class InterestSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "interest_id",
		foreignKey = @ForeignKey(name = "fk_subscription_interest"))
	private Interest interest;

/*	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id",
		foreignKey = @ForeignKey(name = "fk_subscription_user"))
	private User user;*/

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

}