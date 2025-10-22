package com.codeit.monew.interest.domain;

import com.codeit.monew.common.base.BaseDomain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(name = "interest_subscriptions")
@AllArgsConstructor
@NoArgsConstructor
public class InterestSubscription extends BaseDomain {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "interest_id",
		foreignKey = @ForeignKey(name = "fk_subscription_interest"))
	private Interest interest;

/*	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id",
		foreignKey = @ForeignKey(name = "fk_subscription_user"))
	private User user;*/
}