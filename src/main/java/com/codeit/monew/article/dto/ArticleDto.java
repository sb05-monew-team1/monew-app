package com.codeit.monew.article.dto;

import java.time.Instant;
import java.util.UUID;

public record ArticleDto(
	UUID id,
	String source,
	String sourceUrl,
	String title,
	Instant publishDate,
	String summary,
	long commentCount,
	long viewCount,
	boolean viewedByMe
) {
}
