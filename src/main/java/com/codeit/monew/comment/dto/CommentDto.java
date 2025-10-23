package com.codeit.monew.comment.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * 댓글 응답 DTO
 */
public record CommentDto(
	UUID id,
	UUID articleId,
	UUID userId,
	String userNickname,
	String content,
	Long likeCount,
	Boolean likedByMe,
	Instant createdAt
) {
}
