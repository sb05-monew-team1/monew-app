package com.codeit.monew.article.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.dto.ArticleDto;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

	@Mapping(target = "commentCount", expression = "java((long) article.getComments().size())")
	@Mapping(target = "viewCount", expression = "java((long) article.getArticleViews().size())")
	ArticleDto toArticleDto(Article article, boolean viewedByMe);
}
