package com.codeit.monew.article.service;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.domain.ArticleView;
import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.mapper.ArticleViewMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.user.domain.User;
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
		// User 도메인과 관련 커스텀 예외가 작성되면 마무리
		if (!userRepository.existsById(request.monewRequestUserId())) {
			throw new NoSuchElementException();
		}

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

	@Transactional
	public ArticleViewDto registerArticleView(UUID articleId, UUID userId) {
		Article article = articleRepository.findById(articleId)
			.orElseThrow(NoSuchElementException::new); // 임시 예외

		User user = userRepository.findById(userId).orElseThrow(NoSuchElementException::new);

		if (articleViewRepository.existsByUserIdAndArticleId(userId, articleId)) {
			throw new IllegalStateException("Article view already exists");
		}

		ArticleView articleView = ArticleView.builder()
			.user(user)
			.article(article)
			.build();
		articleViewRepository.save(articleView);
		ArticleViewDto dto = articleViewMapper.toDto(articleView);

		return dto;
	}
}
