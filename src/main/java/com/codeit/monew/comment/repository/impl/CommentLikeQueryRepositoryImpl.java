package com.codeit.monew.comment.repository.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.codeit.monew.article.domain.QArticle;
import com.codeit.monew.comment.domain.QComment;
import com.codeit.monew.comment.domain.QCommentLike;
import com.codeit.monew.comment.dto.CommentLikeActivityDto;
import com.codeit.monew.comment.repository.CommentLikeQueryRepository;
import com.codeit.monew.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommentLikeQueryRepositoryImpl implements CommentLikeQueryRepository {

	private final JPAQueryFactory queryFactory;
	private static final QCommentLike cl = QCommentLike.commentLike;
	private static final QComment c = QComment.comment;
	private static final QArticle a = QArticle.article;
	private static final QUser u = QUser.user;

	@Override
	public List<CommentLikeActivityDto> searchRecentCommentLikes(UUID userId) {

		return queryFactory.
			select(Projections.constructor(
				CommentLikeActivityDto.class,
				cl.id,
				cl.createdAt,
				c.id,
				a.id,
				a.title,
				c.user.id,
				c.user.nickname,
				c.content,
				c.likeCount,
				c.createdAt
			))
			.from(cl)
			.join(cl.comment, c)
			.leftJoin(c.article, a)
			.leftJoin(c.user, u)
			.where(cl.user.id.eq(userId))
			.orderBy(cl.createdAt.desc())
			.limit(10)
			.fetch();
	}
}
