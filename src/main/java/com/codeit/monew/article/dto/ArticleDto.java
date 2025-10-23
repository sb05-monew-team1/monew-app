package com.codeit.monew.article.dto;

import java.time.Instant;
import java.util.UUID;

import com.codeit.monew.article.domain.ArticleSource;

public record ArticleDto(
	UUID id,
	ArticleSource source,
	String sourceUrl,
	String title,
	Instant publishDate,
	String summary,
	long commentCount,
	long viewCount,
	boolean viewedByMe
) {
}
