package com.codeit.monew.comment.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 댓글 등록 요청 DTO
 */
public record CommentRegisterRequest(
	@NotNull(message = "기사 ID는 필수입니다")
	UUID articleId,

	@NotNull(message = "사용자 ID는 필수입니다")
	UUID userId,

	@NotBlank(message = "댓글 내용은 필수입니다")
	@Size(min = 1, max = 500, message = "댓글 내용은 1자 이상 500자 이하여야 합니다")
	String content
) {
}
