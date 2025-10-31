package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.dto.ArticleSearchRequestFromService;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.common.config.QuerydslConfig;
import com.querydsl.core.types.Order;

@DataJpaTest
@ActiveProfiles("test")
@Import(QuerydslConfig.class)
public class ArticleRepositoryTest {

	private static final UUID USER_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Autowired
	private ArticleRepository articleRepository;

	@Test
	void searchOrdersByPublishDateDescAndMarksViewedArticles() {
		ArticleSearchRequest request = new ArticleSearchRequest(
			null,
			null,
			null,
			null,
			null,
			"publishDate",
			Order.DESC,
			null,
			null,
			5,
			USER_1
		);

		Slice<ArticleDto> slice = articleRepository.search(ArticleSearchRequestFromService.from(request)).slice();

		assertThat(slice.hasNext()).isTrue();
		assertThat(slice.getContent()).hasSize(5);
		assertThat(slice.getContent())
			.extracting(ArticleDto::id)
			.containsExactly(
				uuid("10000000-0000-0000-0000-00000000000c"),
				uuid("10000000-0000-0000-0000-00000000000b"),
				uuid("10000000-0000-0000-0000-00000000000a"),
				uuid("10000000-0000-0000-0000-000000000009"),
				uuid("10000000-0000-0000-0000-000000000008")
			);

		ArticleDto latest = slice.getContent().get(0);
		ArticleDto second = slice.getContent().get(1);

		assertThat(latest.commentCount()).isEqualTo(1L);
		assertThat(latest.viewCount()).isEqualTo(3L);
		assertThat(latest.viewedByMe()).isFalse();

		assertThat(second.commentCount()).isEqualTo(2L);
		assertThat(second.viewCount()).isEqualTo(3L);
		assertThat(second.viewedByMe()).isTrue();
	}

	@Test
	void searchOrdersByCommentCountAndAppliesCursorForNextPage() {
		ArticleSearchRequest firstPageRequest = new ArticleSearchRequest(
			null,
			null,
			null,
			null,
			null,
			"commentCount",
			Order.DESC,
			null,
			null,
			3,
			USER_1
		);

		Slice<ArticleDto> firstPage = articleRepository.search(ArticleSearchRequestFromService.from(firstPageRequest))
			.slice();

		assertThat(firstPage.hasNext()).isTrue();
		assertThat(firstPage.getContent())
			.extracting(ArticleDto::id)
			.containsExactly(
				uuid("10000000-0000-0000-0000-000000000005"),
				uuid("10000000-0000-0000-0000-000000000001"),
				uuid("10000000-0000-0000-0000-000000000003")
			);
		assertThat(firstPage.getContent())
			.extracting(ArticleDto::commentCount)
			.containsExactly(6L, 5L, 4L);

		ArticleDto lastOfFirstPage = firstPage.getContent().get(2);
		String cursor = String.valueOf(lastOfFirstPage.commentCount());
		Instant after = lastOfFirstPage.publishDate();

		ArticleSearchRequest secondPageRequest = new ArticleSearchRequest(
			null,
			null,
			null,
			null,
			null,
			"commentCount",
			Order.DESC,
			cursor,
			after,
			3,
			USER_1
		);

		Slice<ArticleDto> secondPage = articleRepository.search(ArticleSearchRequestFromService.from(secondPageRequest))
			.slice();

		assertThat(secondPage.hasNext()).isTrue();
		assertThat(secondPage.getContent())
			.extracting(ArticleDto::id)
			.containsExactly(
				uuid("10000000-0000-0000-0000-000000000004"),
				uuid("10000000-0000-0000-0000-00000000000b"),
				uuid("10000000-0000-0000-0000-00000000000a")
			);
		assertThat(secondPage.getContent())
			.extracting(ArticleDto::commentCount)
			.containsExactly(3L, 2L, 2L);
		assertThat(secondPage.getContent().get(0).viewedByMe()).isTrue();
	}

	private static UUID uuid(String value) {
		return UUID.fromString(value);
	}
}
