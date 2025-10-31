package com.codeit.monew.comment.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentLikeActivityDto(
	UUID id,
	Instant createdAt,
	UUID commentId,
	UUID articleId,
	String articleTitle,
	UUID commentUserId,
	String commentUserNickname,
	String commentContent,
	long commentLikeCount,
	Instant commentCreatedAt
) {
}
