package com.codeit.monew.article.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.monew.article.domain.ArticleView;
import com.codeit.monew.article.dto.ArticleViewDto;

@Mapper(componentModel = "spring")
public interface ArticleViewMapper {

	@Mapping(target = "viewedBy", source = "user.id")
	@Mapping(target = "articleId", source = "article.id")
	@Mapping(target = "source", source = "article.source")
	@Mapping(target = "sourceUrl", source = "article.sourceUrl")
	@Mapping(target = "articleTitle", source = "article.title")
	@Mapping(target = "articlePublishedDate", source = "article.publishDate")
	@Mapping(target = "articleSummary", source = "article.summary")
	@Mapping(target = "articleCommentCount", expression = "java(articleView.getArticle().getComments().size())")
	@Mapping(target = "articleViewCount", expression = "java(articleView.getArticle().getArticleViews().size())")
	ArticleViewDto toDto(ArticleView articleView);
}
