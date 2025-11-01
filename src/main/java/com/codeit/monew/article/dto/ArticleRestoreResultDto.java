package com.codeit.monew.article.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ArticleRestoreResultDto(
	LocalDateTime restoreDate,
	List<UUID> restoredArticleIds,
	long restoredArticleCount
) {
}
