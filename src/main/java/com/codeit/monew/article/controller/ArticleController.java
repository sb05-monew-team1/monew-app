package com.codeit.monew.article.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.common.dto.CursorPageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

	private final ArticleService articleService;

	@GetMapping
	public CursorPageResponse<ArticleDto> getArticles(
		@ModelAttribute @Valid ArticleSearchRequest request
	) {
		ArticleSearchRequest filtered = ArticleSearchRequest.filter(request);
		return articleService.search(filtered);
	}
}
