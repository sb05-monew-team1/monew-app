package com.codeit.monew.activity.repository;

import java.util.UUID;

import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.comment.dto.CommentActivityDto;
import com.codeit.monew.comment.dto.CommentLikeActivityDto;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.user.domain.User;

public interface UserActivityQueryRepository {

	void createUserActivity(User user);

	void addSubscription(UUID userId, SubscriptionDto subscription);

	void removeSubscription(UUID userId, UUID interestId);

	void addComment(UUID userId, CommentActivityDto comment);

	void removeComment(UUID userId, UUID commentId);

	void addCommentLike(UUID userId, CommentLikeActivityDto commentLike);

	void removeCommentLike(UUID userId, UUID commentLikeId);

	void addArticleView(UUID userId, ArticleViewDto articleView);
}
