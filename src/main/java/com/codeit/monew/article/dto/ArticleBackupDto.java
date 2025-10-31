package com.codeit.monew.article.dto;

import java.time.Instant;
import java.util.UUID;

public record ArticleBackupDto(
	UUID id,
	String source,
	String sourceUrl,
	String title,
	Instant publishDate,
	String summary,
	int commentCount,
	int viewCount,
	Instant collectedAt,
	Instant createdAt,
	Instant updatedAt,
	Instant deletedAt
) {
}
