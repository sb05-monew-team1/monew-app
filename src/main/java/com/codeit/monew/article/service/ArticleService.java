package com.codeit.monew.article.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.domain.ArticleView;
import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.article.exception.ArticleNotFoundException;
import com.codeit.monew.article.exception.ArticleViewAlreadyExistException;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.mapper.ArticleViewMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

	private final ArticleRepository articleRepository;
	private final PageResponseMapper pageResponseMapper;
	private final UserRepository userRepository;
	private final ArticleViewRepository articleViewRepository;

	private final ArticleMapper articleMapper;
	private final ArticleViewMapper articleViewMapper;

	@Transactional(readOnly = true)
	public CursorPageResponse<ArticleDto> search(ArticleSearchRequest request) {
		validateUser(request.monewRequestUserId());

		Slice<ArticleDto> slice = articleRepository.search(request);
		String nextCursor = null;
		String nextAfter = null;

		if (slice.hasNext() && slice.getNumberOfElements() > 0) {
			ArticleDto last = slice.getContent().get(slice.getNumberOfElements() - 1);

			nextAfter = last.publishDate().toString();
			nextCursor = switch (request.orderBy()) {
				case "commentCount" -> String.valueOf(last.commentCount());
				case "viewCount" -> String.valueOf(last.viewCount());
				default -> nextAfter;
			};
		}

		return pageResponseMapper.toCursorPageResponse(slice, nextCursor, nextAfter);
	}

	@Transactional(readOnly = true)
	public ArticleDto search(UUID articleId, UUID userId) {
		Article article = validateArticle(articleId);
		validateUser(userId);

		boolean viewedByMe = articleViewRepository.existsByUserIdAndArticleId(userId, articleId);

		return articleMapper.toArticleDto(article, viewedByMe);
	}

	@Transactional
	public ArticleViewDto registerArticleView(UUID articleId, UUID userId) {
		Article article = validateArticle(articleId);
		User user = validateUser(userId);

		if (articleViewRepository.existsByUserIdAndArticleId(userId, articleId)) {
			throw new ArticleViewAlreadyExistException().addDetail("articleId", articleId).addDetail("userId", userId);
		}

		ArticleView articleView = ArticleView.builder()
			.user(user)
			.article(article)
			.build();
		articleViewRepository.save(articleView);
		ArticleViewDto dto = articleViewMapper.toDto(articleView);

		return dto;
	}

	@Transactional
	public void deleteSoft(UUID articleId) {
		Article article = validateArticle(articleId);
		article.deleteSoft(Instant.now());
	}

	@Transactional
	public void deleteHard(UUID articleId) {
		// validateArticle 메소드의 경우 논리 삭제된 기사까지 검증하기 때문에 물리 삭제에선 검증 로직 따로 작성
		if(!articleRepository.existsById(articleId)) {
			throw new ArticleNotFoundException().addDetail("articleId", articleId);
		}

		articleRepository.deleteById(articleId);
	}

	private Article validateArticle(UUID articleId) {
		Article article = articleRepository.findById(articleId)
			.orElseThrow(() -> new ArticleNotFoundException().addDetail("articleId", articleId));
		if (article.getDeleted_at() != null) {
			throw new ArticleNotFoundException().addDetail("articleId", articleId);
		}

		return article;
	}

	private User validateUser(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException().addDetail("userId", userId));
	}
}
