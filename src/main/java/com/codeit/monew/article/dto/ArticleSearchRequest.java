package com.codeit.monew.article.dto;

import static com.codeit.monew.common.util.SearchRequestNormalizer.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.codeit.monew.article.domain.ArticleSource;
import com.querydsl.core.types.Order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ArticleSearchRequest(
	String keyword,
	UUID interestId,
	List<ArticleSource> sourceIn,
	Instant publishDateFrom,
	Instant publishDateTo,

	@NotBlank
	String orderBy,

	@NotBlank
	Order direction,

	String cursor,
	Instant after,

	@NotNull
	Integer limit,

	@NotNull
	UUID monewRequestUserId
) {

	public static ArticleSearchRequest filter(ArticleSearchRequest r) {
		int limit = clampSize(r.limit, 50, 10, 100);
		Set<String> allowed = Set.of("publishDate", "commentCount", "viewCount");
		String orderBy = normalizeOrderBy(r.orderBy, allowed, "publishDate");

		return new ArticleSearchRequest(
			r.keyword,
			r.interestId,
			r.sourceIn,
			r.publishDateFrom,
			r.publishDateTo,
			orderBy,
			r.direction,
			r.cursor,
			r.after,
			limit,
			r.monewRequestUserId
		);
	}
}
