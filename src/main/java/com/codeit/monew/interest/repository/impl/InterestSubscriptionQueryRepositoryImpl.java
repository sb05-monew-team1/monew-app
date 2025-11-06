package com.codeit.monew.interest.repository.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.codeit.monew.interest.domain.QInterest;
import com.codeit.monew.interest.domain.QInterestKeyword;
import com.codeit.monew.interest.domain.QInterestSubscription;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.repository.InterestSubscriptionQueryRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InterestSubscriptionQueryRepositoryImpl implements InterestSubscriptionQueryRepository {

	private final JPAQueryFactory queryFactory;
	private static final QInterestSubscription is = QInterestSubscription.interestSubscription;
	private static final QInterest i = QInterest.interest;
	private static final QInterestKeyword ik = QInterestKeyword.interestKeyword;

	@Override
	public List<SubscriptionDto> searchSubsCription(UUID userId) {

		List<Tuple> subscriptionData = queryFactory
			.select(
				is.id,
				i.id,
				i.name,
				i.subscriberCount,
				is.createdAt
			)
			.from(is)
			.join(is.interest, i)
			.where(is.user.id.eq(userId))
			.orderBy(is.createdAt.desc())
			.fetch();

		List<UUID> interestIds = subscriptionData.stream()
			.map(t -> t.get(i.id))
			.toList();

		if (interestIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Tuple> keywordsData = queryFactory
			.select(ik.interest.id, ik.keyword)
			.from(ik)
			.where(ik.interest.id.in(interestIds))
			.fetch();

		Map<UUID, List<String>> interestKeywordsMap = keywordsData.stream()
			.collect(Collectors.groupingBy(
				t -> t.get(0, UUID.class),
				Collectors.mapping(t -> t.get(1, String.class), Collectors.toList())
			));

		return subscriptionData.stream()
			.map(t -> new SubscriptionDto(
				t.get(is.id),
				t.get(i.id),
				t.get(i.name),
				interestKeywordsMap.getOrDefault(t.get(i.id), Collections.emptyList()),
				t.get(i.subscriberCount),
				t.get(is.createdAt)
			))
			.toList();
	}
}
