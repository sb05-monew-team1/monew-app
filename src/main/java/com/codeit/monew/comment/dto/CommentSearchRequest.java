package com.codeit.monew.comment.dto;

import static com.codeit.monew.common.util.SearchRequestNormalizer.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.querydsl.core.types.Order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 댓글 목록 조회 요청 DTO
 */
public record CommentSearchRequest(
	@NotNull(message = "기사 ID를 입력 해 주세요.")
	UUID articleId,

	@NotBlank(message = "정렬 기준을 입력 해 주세요.")
	String orderBy,

	@NotNull(message = "정렬 방향을 입력 해 주세요.")
	Order direction,

	String cursor,
	Instant after,

	@NotNull(message = "페이지 크기를 입력 해 주세요.")
	Integer limit,

	UUID monewRequestUserId
) {

	public static CommentSearchRequest filter(CommentSearchRequest r, UUID monewRequestUserId) {
		int limit = clampSize(r.limit, 10, 1, 100);
		Set<String> allowed = Set.of("date", "likeCount");
		String orderBy = normalizeOrderBy(r.orderBy, allowed, "date");

		return new CommentSearchRequest(
			r.articleId,
			orderBy,
			r.direction,
			r.cursor,
			r.after,
			limit,
			monewRequestUserId
		);
	}
}
