package com.codeit.monew.article.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArticleRestoreResultDto(
	Instant restoreDate,
	List<UUID> restoredArticleIds,
	long restoredArticleCount
) {
}
