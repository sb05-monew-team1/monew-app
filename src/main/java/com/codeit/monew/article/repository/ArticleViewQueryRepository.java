package com.codeit.monew.article.repository;

import java.util.List;
import java.util.UUID;

import com.codeit.monew.article.dto.ArticleViewDto;

public interface ArticleViewQueryRepository {
	List<ArticleViewDto> searchRecentArticleViews(UUID userId);
}
