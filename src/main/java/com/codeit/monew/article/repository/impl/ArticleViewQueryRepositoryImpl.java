package com.codeit.monew.article.repository.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.codeit.monew.article.domain.QArticle;
import com.codeit.monew.article.domain.QArticleView;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.article.repository.ArticleViewQueryRepository;
import com.codeit.monew.comment.domain.QComment;
import com.codeit.monew.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ArticleViewQueryRepositoryImpl implements ArticleViewQueryRepository {

	private final JPAQueryFactory queryFactory;

	private static final QArticleView av = QArticleView.articleView;
	private static final QArticle a = QArticle.article;
	private static final QComment c = QComment.comment;
	private static final QUser u = QUser.user;

	@Override
	public List<ArticleViewDto> searchRecentArticleViews(UUID userId) {

		return queryFactory.
			select(
				Projections.constructor(
					ArticleViewDto.class,
					av.id,
					u.id,
					av.createdAt,
					a.id,
					a.source,
					a.sourceUrl,
					a.title,
					a.publishDate,
					a.summary,
					c.id.countDistinct(),
					av.id.countDistinct()
				))
			.from(av)
			.join(av.article, a)
			.leftJoin(a.comments, c)
			.leftJoin(c.user, u)
			.where(u.id.eq(userId))
			.groupBy(u.id, av.createdAt, a.id, a.source, a.sourceUrl, a.title, a.publishDate, a.summary)
			.orderBy(av.createdAt.desc())
			.limit(10)
			.fetch();
	}
}
