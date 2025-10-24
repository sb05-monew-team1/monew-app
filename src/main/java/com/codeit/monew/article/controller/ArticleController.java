package com.codeit.monew.article.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.dto.ArticleViewDto;
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
		@ModelAttribute @Valid ArticleSearchRequest request,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		ArticleSearchRequest filtered = ArticleSearchRequest.filter(request, userId);
		return articleService.search(filtered);
	}

	@GetMapping("/{articleId}")
	public ResponseEntity<ArticleDto> getArticle(
		@PathVariable("articleId") UUID articleId,
		@RequestHeader("Monew-Request-User-ID") UUID userId
		) {
		return ResponseEntity.ok(articleService.search(articleId, userId));
	}

	@PostMapping("/{articleId}/article-views")
	public ResponseEntity<ArticleViewDto> firstView(
		@PathVariable UUID articleId,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		ArticleViewDto dto = articleService.registerArticleView(articleId, userId);

		return ResponseEntity.status(HttpStatus.CREATED).body(dto);
	}
}
