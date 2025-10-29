package com.codeit.monew.comment.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * 댓글 좋아요 등록/취소 요청 DTO
 */
public record CommentLikeRequest(
	@NotNull(message = "사용자 ID는 필수입니다")
	UUID userId
) {
}
