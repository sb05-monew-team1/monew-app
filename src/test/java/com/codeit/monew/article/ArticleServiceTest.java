package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.domain.ArticleSource;
import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

	@Mock
	private ArticleRepository articleRepository;

	@Mock
	private ArticleMapper articleMapper;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ArticleViewRepository articleViewRepository;

	@InjectMocks
	private ArticleService articleService;


	@Nested
	class GetArticle {
		@Test
		@DisplayName("기사 단건 조회 성공")
		void success() {
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			Instant date = Instant.now();
			ArticleDto articleDto = new ArticleDto(
				articleId,
				ArticleSource.NAVER,
				"url",
				"title",
				date,
				"summary",
				100L,
				10L,
				true
			);
			Article article = new Article(
				ArticleSource.NAVER,
				"url",
				"title",
				date,
				"summary",
				List.of(),
				List.of(),
				List.of(),
				null
			);

			given(articleRepository.findById(any())).willReturn(Optional.of(article));
			given(userRepository.findById(any())).willReturn(Optional.ofNullable(User.builder().build()));
			given(articleViewRepository.existsByUserIdAndArticleId(any(), any())).willReturn(true);
			given(articleMapper.toArticleDto(any(), eq(true))).willReturn(articleDto);

			ArticleDto result = articleService.search(articleId, userId);

			assertThat(result).isEqualTo(articleDto);


		}
	}




}
