package com.codeit.monew.article.repository.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.codeit.monew.article.domain.QArticle;
import com.codeit.monew.article.domain.QArticleView;
import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequestFromService;
import com.codeit.monew.article.dto.ArticleSearchResultDto;
import com.codeit.monew.article.repository.ArticleQueryRepository;
import com.codeit.monew.comment.domain.QComment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ArticleQueryRepositoryImpl implements ArticleQueryRepository {

	private final JPAQueryFactory queryFactory;
	private static final QArticle a = QArticle.article;
	private static final QComment c = QComment.comment;
	private static final QArticleView articleView = QArticleView.articleView;

	@Override
	public ArticleSearchResultDto search(ArticleSearchRequestFromService req) {

		BooleanBuilder builder = buildFilterCondition(req);

		NumberExpression<Long> commentCountExpr = c.id.countDistinct();
		NumberExpression<Long> viewCountExpr = articleView.id.countDistinct();

		Order order = req.direction();
		OrderSpecifier<?> orderBy = resolveOrderBy(req.orderBy(), order, commentCountExpr, viewCountExpr);
		OrderSpecifier<?> publishDateOrder = order == Order.ASC ? a.publishDate.asc() : a.publishDate.desc();

		CursorConstraint cursorConstraint = buildCursorConstraint(req, order, commentCountExpr, viewCountExpr);
		if (cursorConstraint.whereCondition() != null) {
			builder.and(cursorConstraint.whereCondition());
		}

		QArticleView viewedArticleView = new QArticleView("viewedArticle");
		BooleanExpression viewedByMeExpr = JPAExpressions
			.selectOne()
			.from(viewedArticleView)
			.where(viewedArticleView.article.eq(a)
				.and(viewedArticleView.user.id.eq(req.userId())))
			.exists();

		int limit = req.limit();

		var query = queryFactory
			.select(
				a.id,
				a.source,
				a.sourceUrl,
				a.title,
				a.publishDate,
				a.summary,
				commentCountExpr,
				viewCountExpr,
				viewedByMeExpr,
				a.createdAt
			)
			.from(a)
			.distinct()
			.leftJoin(a.comments, c).on(c.deletedAt.isNull())
			.leftJoin(a.articleViews, articleView)
			.where(builder)
			.groupBy(a.id, a.source, a.sourceUrl, a.title, a.publishDate, a.summary, a.createdAt);

		if (cursorConstraint.havingCondition() != null) {
			query.having(cursorConstraint.havingCondition());
		}

		List<Tuple> rowsPlusOne = query
			.orderBy(orderBy, publishDateOrder)
			.limit(limit + 1L)
			.fetch();

		boolean hasNext = rowsPlusOne.size() > limit;
		List<Tuple> contentTuples = hasNext ? rowsPlusOne.subList(0, limit) : rowsPlusOne;

		List<ArticleDto> content = contentTuples.stream()
			.map(tuple -> {
				Long commentCount = Objects.requireNonNullElse(tuple.get(commentCountExpr), 0L);
				Long viewCount = Objects.requireNonNullElse(tuple.get(viewCountExpr), 0L);

				return new ArticleDto(
					tuple.get(a.id),
					tuple.get(a.source),
					tuple.get(a.sourceUrl),
					tuple.get(a.title),
					tuple.get(a.publishDate),
					tuple.get(a.summary),
					commentCount,
					viewCount,
					Boolean.TRUE.equals(tuple.get(viewedByMeExpr))
				);
			})
			.toList();

		Instant lastCreatedAt = contentTuples.isEmpty()
			? null
			: contentTuples.get(contentTuples.size() - 1).get(a.createdAt);

		Pageable pageable = Pageable.ofSize(limit);
		return new ArticleSearchResultDto(new SliceImpl<>(content, pageable, hasNext), lastCreatedAt);
	}

	private BooleanBuilder buildFilterCondition(ArticleSearchRequestFromService req) {
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(a.deletedAt.isNull());

		if (req.keyword() != null && !req.keyword().isBlank()) {
			builder.and(a.title.containsIgnoreCase(req.keyword())
				.or(a.summary.containsIgnoreCase(req.keyword())));
		}

		if (req.interestId() != null) {
			builder.and(a.interests.any().id.eq(req.interestId()));
		}

		if (req.sourceIn() != null && !req.sourceIn().isEmpty()) {
			builder.and(a.source.in(req.sourceIn()));
		}

		if (req.publishDateFrom() != null && req.publishDateTo() != null) {
			builder.and(a.publishDate.goe(req.publishDateFrom())
				.and(a.publishDate.loe(req.publishDateTo())));
		}

		return builder;
	}

	private OrderSpecifier<?> resolveOrderBy(String orderBy, Order order,
		NumberExpression<Long> commentCountExpr, NumberExpression<Long> viewCountExpr) {

		return switch (orderBy) {
			case "commentCount" -> new OrderSpecifier<>(order, commentCountExpr);
			case "viewCount" -> new OrderSpecifier<>(order, viewCountExpr);
			default -> new OrderSpecifier<>(order, a.publishDate);
		};
	}

	private CursorConstraint buildCursorConstraint(ArticleSearchRequestFromService req, Order order,
		NumberExpression<Long> commentCountExpr, NumberExpression<Long> viewCountExpr) {

		if (req.cursor() == null || req.cursor().isBlank() || req.after() == null) {
			return CursorConstraint.EMPTY;
		}

		return switch (req.orderBy()) {
			case "commentCount" -> buildCountCursor(commentCountExpr, req.cursor(), req.after(), order);
			case "viewCount" -> buildCountCursor(viewCountExpr, req.cursor(), req.after(), order);
			case "publishDate" -> buildPublishDateCursor(req.cursor(), req.after(), order);
			default -> CursorConstraint.EMPTY;
		};
	}

	private CursorConstraint buildCountCursor(NumberExpression<Long> metricExpr, String cursor,
		Instant after, Order order) {
		long cursorValue = Long.parseLong(cursor);
		BooleanExpression metricComparison = order == Order.DESC
			? metricExpr.lt(cursorValue)
			: metricExpr.gt(cursorValue);
		BooleanExpression tieBreak = order == Order.DESC
			? metricExpr.eq(cursorValue).and(a.createdAt.lt(after))
			: metricExpr.eq(cursorValue).and(a.createdAt.gt(after));
		return new CursorConstraint(null, metricComparison.or(tieBreak));
	}

	private CursorConstraint buildPublishDateCursor(String cursor, Instant after, Order order) {
		Instant cursorInstant = Instant.parse(cursor);
		BooleanExpression publishComparison = order == Order.DESC
			? a.publishDate.lt(cursorInstant)
			: a.publishDate.gt(cursorInstant);
		BooleanExpression tieBreak = order == Order.DESC
			? a.publishDate.eq(cursorInstant).and(a.createdAt.lt(after))
			: a.publishDate.eq(cursorInstant).and(a.createdAt.gt(after));
		return new CursorConstraint(publishComparison.or(tieBreak), null);
	}

	private record CursorConstraint(BooleanExpression whereCondition, BooleanExpression havingCondition) {
		private static final CursorConstraint EMPTY = new CursorConstraint(null, null);
	}

}
