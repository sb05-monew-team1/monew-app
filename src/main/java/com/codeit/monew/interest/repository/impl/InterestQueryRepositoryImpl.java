package com.codeit.monew.interest.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.QInterest;
import com.codeit.monew.interest.repository.InterestQueryRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InterestQueryRepositoryImpl implements InterestQueryRepository {

	private static final QInterest interest = QInterest.interest;

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Interest> findAllWithOrders(
		Predicate predicate,
		List<OrderSpecifier<?>> orderSpecifiers,
		int limit
	) {
		var query = queryFactory.selectFrom(interest);

		if (predicate != null) {
			query.where(predicate);
		}

		if (orderSpecifiers != null && !orderSpecifiers.isEmpty()) {
			query.orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new));
		}

		return query
			.limit(limit)
			.fetch();
	}
}
