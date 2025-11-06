package com.codeit.monew.article.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.codeit.monew.user.domain.User;

class ArticleViewTest {

	@Test
	void updateSetsUpdatedAt() {
		ArticleView articleView = ArticleView.builder()
			.user(mock(User.class))
			.article(mock(Article.class))
			.build();

		Instant now = Instant.now();

		articleView.update(now);

		assertThat(articleView.getUpdatedAt()).isEqualTo(now);
	}
}
