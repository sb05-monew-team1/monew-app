package com.codeit.monew.comment.repository.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.codeit.monew.article.domain.QArticle;
import com.codeit.monew.comment.domain.QComment;
import com.codeit.monew.comment.dto.CommentActivityDto;
import com.codeit.monew.comment.repository.CommentQueryRepository;
import com.codeit.monew.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

	private final JPAQueryFactory queryFactory;
	private static final QComment c = QComment.comment;
	private static final QArticle a = QArticle.article;
	private static final QUser u = QUser.user;


	@Override
	public List<CommentActivityDto> searchRecentComments(UUID userId) {

		return queryFactory.
			select(Projections.constructor(
				CommentActivityDto.class,
				c.id, a.id, a.title,
				u.id, u.nickname, c.content,
				c.likeCount, c.createdAt))
			.from(c)
			.join(c.user, u)
			.leftJoin(c.article, a)
			.where(c.user.id.eq(userId))
			.orderBy(c.createdAt.desc())
			.limit(10)
			.fetch();
	}
}
