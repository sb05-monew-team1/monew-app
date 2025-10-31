package com.codeit.monew.article.dto;

import java.time.Instant;

import org.springframework.data.domain.Slice;

public record ArticleSearchResultDto(
	Slice<ArticleDto> slice,
	Instant createdAt
) {
}
