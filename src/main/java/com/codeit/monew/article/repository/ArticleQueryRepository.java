package com.codeit.monew.article.repository;

import org.springframework.data.domain.Slice;

import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;

public interface ArticleQueryRepository {

	Slice<ArticleDto> search(ArticleSearchRequest request);
}
