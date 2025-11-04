package com.codeit.monew.article.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleRestoreResultDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.common.dto.CursorPageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

	private final ArticleService articleService;

	@GetMapping
	public CursorPageResponse<ArticleDto> getArticles(
		@ModelAttribute @Valid ArticleSearchRequest request,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		log.info("기사 목록 조회 요청 userId={} orderBy={} direction={} cursor={} limit={}",
			userId, request.orderBy(), request.direction(), request.cursor(), request.limit());

		ArticleSearchRequest filtered = ArticleSearchRequest.filter(request, userId);
		log.debug("기사 검색 파라미터 정규화 orderBy={} cursor={} after={} limit={}",
			filtered.orderBy(), filtered.cursor(), filtered.after(), filtered.limit());

		return articleService.search(filtered);
	}

	@GetMapping("/{articleId}")
	public ResponseEntity<ArticleDto> getArticle(
		@PathVariable("articleId") UUID articleId,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		log.info("기사 단건 조회 요청 articleId={} userId={}", articleId, userId);

		return ResponseEntity.ok(articleService.search(articleId, userId));
	}

	@GetMapping("/sources")
	public ResponseEntity<List<String>> getSources() {
		log.info("기사 출처 목록 조회 요청");

		return ResponseEntity.ok(articleService.getSources());
	}

	@PostMapping("/{articleId}/article-views")
	public ResponseEntity<ArticleViewDto> firstView(
		@PathVariable UUID articleId,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		log.info("기사 첫 뷰 등록 요청 articleId={} userId={}", articleId, userId);

		ArticleViewDto dto = articleService.registerArticleView(articleId, userId);

		return ResponseEntity.status(HttpStatus.CREATED).body(dto);
	}

	@DeleteMapping("/{articleId}")
	public ResponseEntity<Void> deleteSoft(
		@PathVariable UUID articleId
	) {
		log.info("기사 논리 삭제 요청 articleId={}", articleId);

		articleService.deleteSoft(articleId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@DeleteMapping("/{articleId}/hard")
	public ResponseEntity<Void> deleteHard(
		@PathVariable UUID articleId
	) {
		log.info("기사 물리 삭제 요청 articleId={}", articleId);

		articleService.deleteHard(articleId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/restore")
	public ResponseEntity<ArticleRestoreResultDto> restore(
		@RequestParam(name = "from") LocalDateTime from,
		@RequestParam(name = "to") LocalDateTime to
	) {
		log.info("기사 백업 복원 요청 from={} to={}", from, to);

		return ResponseEntity.ok(articleService.restore(from, to));
	}
}
