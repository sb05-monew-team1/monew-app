package com.codeit.monew.article.repository;

import com.codeit.monew.article.dto.ArticleSearchRequestFromService;
import com.codeit.monew.article.dto.ArticleSearchResultDto;

public interface ArticleQueryRepository {

	ArticleSearchResultDto search(ArticleSearchRequestFromService request);
}
