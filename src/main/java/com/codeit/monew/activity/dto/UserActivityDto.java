package com.codeit.monew.activity.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.comment.dto.CommentActivityDto;
import com.codeit.monew.comment.dto.CommentLikeActivityDto;
import com.codeit.monew.interest.dto.SubscriptionDto;

public record UserActivityDto(
	UUID id,
	String email,
	String nickname,
	Instant createdAt,
	List<SubscriptionDto> subscriptions,
	List<CommentActivityDto> comments,
	List<CommentLikeActivityDto> commentLikes,
	List<ArticleViewDto> articleViews
) {
}
