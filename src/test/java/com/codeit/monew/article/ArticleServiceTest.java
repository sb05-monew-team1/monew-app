package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.domain.ArticleSource;
import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.dto.ArticleSearchResultDto;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.repository.InterestSubscriptionRepository;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;
import com.querydsl.core.types.Order;

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

	@Mock
	private InterestSubscriptionRepository interestSubscriptionRepository;

	@Mock
	private PageResponseMapper pageResponseMapper;

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
				"금리 인상 뉴스",
				date,
				"금리와 물가 뉴스",
				100L,
				10L,
				true
			);
			Article article = new Article();
			SubscriptionDto subscriptionDto = new SubscriptionDto(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"금리",
				List.of("금리", "뉴스"),
				1000L,
				Instant.now()
			);

			given(articleRepository.findById(any())).willReturn(Optional.of(article));
			given(userRepository.findById(any())).willReturn(Optional.ofNullable(User.builder().build()));
			given(articleViewRepository.existsByUserIdAndArticleId(any(), any())).willReturn(true);
			given(articleMapper.toArticleDto(any(), eq(true))).willReturn(articleDto);
			given(interestSubscriptionRepository.searchSubsCription(any())).willReturn(List.of(subscriptionDto));

			ArticleDto result = articleService.search(articleId, userId);

			ArticleDto expected = new ArticleDto(
				articleId,
				ArticleSource.NAVER,
				"url",
				"<b>금리</b> 인상 <b>뉴스</b>",
				date,
				"<b>금리</b>와 물가 <b>뉴스</b>",
				100L,
				10L,
				true
			);

			assertThat(result).isEqualTo(expected);

		}

		@Test
		@DisplayName("이미 강조된 텍스트는 중복으로 감싸지지 않는다")
		void highlightSkipsExistingBold() {
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			Instant date = Instant.now();
			ArticleDto articleDto = new ArticleDto(
				articleId,
				ArticleSource.NAVER,
				"url",
				"<b>금리</b> 동향과 금리 전망",
				date,
				"<strong>금리</strong> 정책과 금리 분석",
				10L,
				5L,
				false
			);
			Article article = new Article();
			SubscriptionDto subscriptionDto = new SubscriptionDto(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"금리",
				List.of("금리"),
				500L,
				Instant.now()
			);

			given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
			given(userRepository.findById(userId)).willReturn(Optional.ofNullable(User.builder().build()));
			given(articleViewRepository.existsByUserIdAndArticleId(userId, articleId)).willReturn(false);
			given(articleMapper.toArticleDto(article, false)).willReturn(articleDto);
			given(interestSubscriptionRepository.searchSubsCription(userId)).willReturn(List.of(subscriptionDto));

			ArticleDto result = articleService.search(articleId, userId);

			assertThat(result.title()).isEqualTo("<b>금리</b> 동향과 <b>금리</b> 전망");
			assertThat(result.summary()).isEqualTo("<strong>금리</strong> 정책과 <b>금리</b> 분석");
		}
	}

	@Nested
	class SearchArticles {
		@Test
		@DisplayName("관심사 선택 시 해당 관심사 키워드만 강조")
		void highlightOnlySelectedInterestKeywords() {
			UUID userId = UUID.randomUUID();
			UUID targetInterestId = UUID.randomUUID();
			UUID otherInterestId = UUID.randomUUID();

			ArticleSearchRequest request = new ArticleSearchRequest(
				null,
				targetInterestId,
				List.of(),
				null,
				null,
				"publishDate",
				Order.DESC,
				null,
				null,
				10,
				userId
			);

			ArticleDto articleDto = new ArticleDto(
				UUID.randomUUID(),
				ArticleSource.NAVER,
				"url",
				"미국 주식 시장과 금융 동향",
				Instant.now(),
				"해외 주식 투자와 금융 이슈 정리",
				0L,
				0L,
				false
			);

			Slice<ArticleDto> articleSlice = new SliceImpl<>(List.of(articleDto), Pageable.ofSize(10), false);
			ArticleSearchResultDto searchResult = new ArticleSearchResultDto(articleSlice, null);

			SubscriptionDto targetSubscription = new SubscriptionDto(
				UUID.randomUUID(),
				targetInterestId,
				"주식",
				List.of("주식"),
				1_000L,
				Instant.now()
			);

			SubscriptionDto otherSubscription = new SubscriptionDto(
				UUID.randomUUID(),
				otherInterestId,
				"금융",
				List.of("금융"),
				2_000L,
				Instant.now()
			);

			given(userRepository.findById(userId)).willReturn(Optional.ofNullable(User.builder()
				.email("test@example.com")
				.nickname("tester")
				.password("secret")
				.build()));
			given(articleRepository.search(any())).willReturn(searchResult);
			given(articleRepository.count()).willReturn(1L);
			given(interestSubscriptionRepository.searchSubsCription(userId))
				.willReturn(List.of(targetSubscription, otherSubscription));
			given(pageResponseMapper.toCursorPageResponse(any(), any(), any(), anyLong()))
				.willAnswer(invocation -> {
					Slice<ArticleDto> highlighted = invocation.getArgument(0);
					assertThat(highlighted.getContent()).hasSize(1);
					ArticleDto highlightedArticle = highlighted.getContent().get(0);
					assertThat(highlightedArticle.title()).contains("<b>주식</b>");
					assertThat(highlightedArticle.title()).doesNotContain("<b>금융</b>");
					assertThat(highlightedArticle.summary()).contains("<b>주식</b>");
					assertThat(highlightedArticle.summary()).doesNotContain("<b>금융</b>");

					return CursorPageResponse.<ArticleDto>builder()
						.content(highlighted.getContent())
						.nextCursor(null)
						.nextAfter(null)
						.size(highlighted.getSize())
						.totalElements(1L)
						.hasNext(highlighted.hasNext())
						.build();
				});

			CursorPageResponse<ArticleDto> response = articleService.search(request);

			assertThat(response.content()).hasSize(1);
			ArticleDto highlighted = response.content().get(0);
			assertThat(highlighted.title()).isEqualTo("미국 <b>주식</b> 시장과 금융 동향");
			assertThat(highlighted.summary()).isEqualTo("해외 <b>주식</b> 투자와 금융 이슈 정리");
		}

		@Test
		@DisplayName("긴 키워드를 먼저 강조하고 중복 키워드를 제거한다")
		void highlightPrioritisesLongerKeywords() {
			UUID userId = UUID.randomUUID();

			ArticleSearchRequest request = new ArticleSearchRequest(
				null,
				null,
				List.of(),
				null,
				null,
				"publishDate",
				Order.ASC,
				null,
				null,
				5,
				userId
			);

			ArticleDto articleDto = new ArticleDto(
				UUID.randomUUID(),
				ArticleSource.NAVER,
				"url",
				"국내 주식 시장 리포트",
				Instant.now(),
				"주식과 주 전망",
				0L,
				0L,
				false
			);

			Slice<ArticleDto> articleSlice = new SliceImpl<>(List.of(articleDto), Pageable.ofSize(5), false);
			ArticleSearchResultDto searchResult = new ArticleSearchResultDto(articleSlice, null);

			SubscriptionDto primary = new SubscriptionDto(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"투자",
				Arrays.asList(" 주식", "주", "주식"),
				300L,
				Instant.now()
			);
			SubscriptionDto emptyKeywords = new SubscriptionDto(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"기본",
				null,
				200L,
				Instant.now()
			);

			given(userRepository.findById(userId)).willReturn(Optional.ofNullable(User.builder()
				.email("another@example.com")
				.nickname("tester")
				.password("secret")
				.build()));
			given(articleRepository.search(any())).willReturn(searchResult);
			given(articleRepository.count()).willReturn(1L);
			given(interestSubscriptionRepository.searchSubsCription(userId))
				.willReturn(new ArrayList<>(Arrays.asList(primary, null, emptyKeywords)));
			given(pageResponseMapper.toCursorPageResponse(any(), any(), any(), anyLong()))
				.willAnswer(invocation -> {
					Slice<ArticleDto> highlighted = invocation.getArgument(0);
					return CursorPageResponse.<ArticleDto>builder()
						.content(highlighted.getContent())
						.nextCursor(invocation.getArgument(1))
						.nextAfter(invocation.getArgument(2))
						.size(highlighted.getNumberOfElements())
						.totalElements(invocation.getArgument(3))
						.hasNext(highlighted.hasNext())
						.build();
				});

			CursorPageResponse<ArticleDto> response = articleService.search(request);

			assertThat(response.content()).hasSize(1);
			ArticleDto highlighted = response.content().get(0);
			assertThat(highlighted.title()).isEqualTo("국내 <b>주식</b> 시장 리포트");
			assertThat(highlighted.summary()).isEqualTo("<b>주식</b>과 <b>주</b> 전망");
		}
	}

}
