package com.codeit.monew.article.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.codeit.monew.article.domain.ArticleSource;
import com.querydsl.core.types.Order;

public record ArticleSearchRequestFromService(
	String keyword,
	UUID interestId,
	List<ArticleSource> sourceIn,
	Instant publishDateFrom,
	Instant publishDateTo,
	String orderBy,
	Order direction,
	String cursor,
	Instant after,
	Integer limit,
	UUID monewRequestUserId
) {
	public static ArticleSearchRequestFromService from(ArticleSearchRequest request) {
		ZoneId zoneId = ZoneId.of("Asia/Seoul");
		Instant fromInstant = request.publishDateFrom() == null ? null
			: request.publishDateFrom().atZone(zoneId).toInstant();
		Instant toInstant = request.publishDateTo() == null ? null
			: request.publishDateTo().atZone(zoneId).toInstant();

		List<ArticleSource> sources = Optional.ofNullable(request.sourceIn())
			.map(values -> values.stream()
				.filter(java.util.Objects::nonNull)
				.flatMap(value -> Arrays.stream(value.split(",")))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(s -> s.toUpperCase(Locale.ROOT))
				.map(ArticleSearchRequestFromService::safeValueOf)
				.flatMap(Optional::stream)
				.distinct()
				.toList())
			.filter(list -> !list.isEmpty())
			.orElse(null);

		return new ArticleSearchRequestFromService(
			request.keyword(),
			request.interestId(),
			sources,
			fromInstant,
			toInstant,
			request.orderBy(),
			request.direction(),
			request.cursor(),
			request.after(),
			request.limit(),
			request.monewRequestUserId()
		);
	}

	private static Optional<ArticleSource> safeValueOf(String value) {
		try {
			return Optional.of(ArticleSource.valueOf(value));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
}
