package com.codeit.monew.article.repository.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.codeit.monew.article.domain.QArticle;
import com.codeit.monew.article.domain.QArticleView;
import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.repository.ArticleQueryRepository;
import com.codeit.monew.comment.domain.QComment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
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
	public Slice<ArticleDto> search(ArticleSearchRequest req) {

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(a.deleted_at.isNull());

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

		NumberExpression<Long> commentCountExpr = c.id.countDistinct();
		NumberExpression<Long> viewCountExpr = articleView.id.countDistinct();

		BooleanBuilder having = new BooleanBuilder();

		Order order = req.direction();
		OrderSpecifier<?> orderBy = switch (req.orderBy()) {
			case "commentCount" -> new OrderSpecifier<>(order, commentCountExpr);
			case "viewCount" -> new OrderSpecifier<>(order, viewCountExpr);
			default -> new OrderSpecifier<>(order, a.publishDate);
		};
		OrderSpecifier<?> publishDateOrder = order == Order.ASC ? a.publishDate.asc() : a.publishDate.desc();

		if (req.cursor() != null && !req.cursor().isBlank() && req.after() != null) {
			Instant after = req.after();

			switch (req.orderBy()) {
				case "commentCount" -> {
					long cursor = Long.parseLong(req.cursor());
					BooleanExpression countPart = order == Order.DESC
						? commentCountExpr.lt(cursor)
						: commentCountExpr.gt(cursor);
					BooleanExpression tieBreak = order == Order.DESC
						? commentCountExpr.eq(cursor).and(a.publishDate.lt(after))
						: commentCountExpr.eq(cursor).and(a.publishDate.gt(after));
					having.and(countPart.or(tieBreak));
				}
				case "viewCount" -> {
					long cursor = Long.parseLong(req.cursor());
					BooleanExpression viewPart = order == Order.DESC
						? viewCountExpr.lt(cursor)
						: viewCountExpr.gt(cursor);
					BooleanExpression tieBreak = order == Order.DESC
						? viewCountExpr.eq(cursor).and(a.publishDate.lt(after))
						: viewCountExpr.eq(cursor).and(a.publishDate.gt(after));
					having.and(viewPart.or(tieBreak));
				}
				case "publishDate" -> {
					Instant cursor = Instant.parse(req.cursor());
					BooleanExpression datePart = order == Order.DESC
						? a.publishDate.lt(cursor)
						: a.publishDate.gt(cursor);
					builder.and(datePart);
				}
			}
		}

		QArticleView viewedArticleView = new QArticleView("viewedArticle");
		BooleanExpression viewedByMeExpr = JPAExpressions
			.selectOne()
			.from(viewedArticleView)
			.where(viewedArticleView.article.eq(a)
				.and(viewedArticleView.user.id.eq(req.monewRequestUserId())))
			.exists();

		int limit = req.limit();

		List<ArticleDto> rowsPlusOne = queryFactory
			.select(Projections.constructor(
				ArticleDto.class,
				a.id,
				a.source,
				a.sourceUrl,
				a.title,
				a.publishDate,
				a.summary,
				commentCountExpr,
				viewCountExpr,
				viewedByMeExpr
			))
			.from(a)
			.distinct()
			.leftJoin(a.comments, c)
			.leftJoin(a.articleViews, articleView)
			.where(builder)
			.groupBy(a.id, a.source, a.sourceUrl, a.title, a.publishDate, a.summary)
			.having(having)
			.orderBy(orderBy, publishDateOrder)
			.limit(limit + 1L)
			.fetch();

		boolean hasNext = rowsPlusOne.size() > limit;
		List<ArticleDto> content = hasNext ? rowsPlusOne.subList(0, limit) : rowsPlusOne;

		Pageable pageable = Pageable.ofSize(limit);
		return new SliceImpl<>(content, pageable, hasNext);
	}

}
