package com.codeit.monew.interest.repository.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.codeit.monew.interest.domain.QInterest;
import com.codeit.monew.interest.domain.QInterestSubscription;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.repository.InterestSubscriptionQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InterestSubscriptionQueryRepositoryImpl implements InterestSubscriptionQueryRepository {

	private final JPAQueryFactory queryFactory;
	private static final QInterestSubscription is = QInterestSubscription.interestSubscription;
	private static final QInterest i = QInterest.interest;

	@Override
	public List<SubscriptionDto> searchSubsCription(UUID userId) {
		return queryFactory.
			select(Projections.constructor(
				SubscriptionDto.class,
				is.id,
				i.id,
				i.name,
				i.keywords,
				i.subscriberCount,
				is.createdAt
			))
			.from(is)
			.join(is.interest, i)
			.where(is.user.id.eq(userId))
			.orderBy(is.createdAt.desc())
			.fetch();
	}
}
