package com.codeit.monew.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.codeit.monew.notification.domain.QNotification;
import com.codeit.monew.notification.dto.NotificationDto;
import com.codeit.monew.user.domain.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

	private final JPAQueryFactory queryFactory;
	private static final QNotification n = QNotification.notification;
	private static final QUser u = QUser.user;

	@Override
	public Slice<NotificationDto> search(
		String cursor,
		Instant after,
		int limit,
		String monewRequestUserId
	) {
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(n.confirmed.eq(false));
		if (monewRequestUserId != null && !monewRequestUserId.isBlank()) {
			UUID userId = UUID.fromString(monewRequestUserId);
			builder.and(n.user.id.eq(userId));
		}
		if (cursor != null && !cursor.isBlank()) {
			Instant cursorTime = Instant.parse(cursor);
			builder.and(n.createdAt.gt(cursorTime));
		} else {
			if (after != null) {
				builder.and(n.createdAt.gt(after));
			}
		}
		OrderSpecifier<?> orderByTime = new OrderSpecifier<>(Order.ASC, n.createdAt);

		List<NotificationDto> rowsPlusOne = queryFactory
			.select(Projections.constructor(
				NotificationDto.class,
				n.id, n.createdAt, n.updatedAt,
				u.id, n.content, n.resourceType,
				n.resourceId
			))
			.from(n)
			.distinct()
			.leftJoin(n.user, u)
			.where(builder)
			.orderBy(orderByTime)
			.limit(limit + 1L)
			.fetch();

		boolean hasNext = rowsPlusOne.size() > limit;
		List<NotificationDto> content = hasNext ? rowsPlusOne.subList(0, limit) : rowsPlusOne;

		Pageable pageable = Pageable.ofSize(limit);
		return new SliceImpl<>(content, pageable, hasNext);
	}

	@Override
	public long countUnConfirmed(String monewRequestUserId) {
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(n.confirmed.eq(false));
		if (monewRequestUserId != null && !monewRequestUserId.isBlank()) {
			UUID userId = UUID.fromString(monewRequestUserId);
			builder.and(n.user.id.eq(userId));
		}

		return queryFactory
			.select(n.id.countDistinct())
			.from(n)
			.where(builder)
			.fetchOne()
			.longValue();
	}
}
