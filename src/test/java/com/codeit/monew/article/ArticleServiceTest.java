package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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

import com.codeit.monew.activity.service.UserActivityService;
import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.domain.ArticleSource;
import com.codeit.monew.article.domain.ArticleView;
import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.dto.ArticleSearchResultDto;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.article.exception.ArticleViewAlreadyExistException;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.mapper.ArticleViewMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.article.service.ArticleStorage;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.repository.InterestSubscriptionRepository;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;
import com.querydsl.core.types.Order;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

	@Mock
	private ArticleRepository articleRepository;

	@Mock
	private ArticleMapper articleMapper;

	@Mock
	private ArticleViewMapper articleViewMapper;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ArticleViewRepository articleViewRepository;

	@Mock
	private InterestSubscriptionRepository interestSubscriptionRepository;

	@Mock
	private PageResponseMapper pageResponseMapper;

	@Mock
	private ArticleStorage articleStorage;

	@Mock
	private UserActivityService userActivityService;

	@Mock
	private MeterRegistry meterRegistry;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private ArticleService articleService;

	private final Map<String, Counter> counters = new HashMap<>();
	private final Map<String, DistributionSummary> summaries = new HashMap<>();

	@BeforeEach
	void setUpMeterRegistry() {
		counters.clear();
		summaries.clear();
		Mockito.lenient().when(meterRegistry.counter(anyString()))
			.thenAnswer(invocation -> counters.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(Counter.class)));
		Mockito.lenient().when(meterRegistry.counter(anyString(), any(String[].class)))
			.thenAnswer(invocation -> counters.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(Counter.class)));
		Mockito.lenient().when(meterRegistry.summary(anyString()))
			.thenAnswer(invocation -> summaries.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(DistributionSummary.class)));
	}

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
					Number totalElements = invocation.getArgument(3, Number.class);
					return CursorPageResponse.<ArticleDto>builder()
						.content(highlighted.getContent())
						.nextCursor(invocation.getArgument(1, String.class))
						.nextAfter(invocation.getArgument(2, String.class))
						.size(highlighted.getNumberOfElements())
						.totalElements(totalElements.longValue())
						.hasNext(highlighted.hasNext())
						.build();
				});

			CursorPageResponse<ArticleDto> response = articleService.search(request);

			assertThat(response.content()).hasSize(1);
			ArticleDto highlighted = response.content().get(0);
			assertThat(highlighted.title()).isEqualTo("국내 <b>주식</b> 시장 리포트");
			assertThat(highlighted.summary()).isEqualTo("<b>주식</b>과 <b>주</b> 전망");
		}

		@Test
		@DisplayName("관심 키워드가 없으면 텍스트를 수정하지 않는다")
		void searchReturnsOriginalContentWhenNoKeywords() {
			UUID userId = UUID.randomUUID();

			ArticleSearchRequest request = new ArticleSearchRequest(
				null,
				null,
				List.of(),
				null,
				null,
				"publishDate",
				Order.DESC,
				null,
				null,
				5,
				userId
			);

			ArticleDto original = new ArticleDto(
				UUID.randomUUID(),
				ArticleSource.NAVER,
				"url",
				"그냥 기사 제목",
				Instant.now(),
				"요약 내용",
				0L,
				0L,
				false
			);

			Slice<ArticleDto> articleSlice = new SliceImpl<>(List.of(original), Pageable.ofSize(5), false);
			ArticleSearchResultDto searchResult = new ArticleSearchResultDto(articleSlice, null);

			SubscriptionDto nullKeywords = new SubscriptionDto(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"빈 관심사",
				List.of("", " "),
				0L,
				Instant.now()
			);

			given(userRepository.findById(userId)).willReturn(Optional.ofNullable(User.builder()
				.email("no-keyword@example.com")
				.nickname("tester")
				.password("secret")
				.build()));
			given(articleRepository.search(any())).willReturn(searchResult);
			given(articleRepository.count()).willReturn(0L);
			given(interestSubscriptionRepository.searchSubsCription(userId))
				.willReturn(Arrays.asList(null, new SubscriptionDto(
					UUID.randomUUID(), UUID.randomUUID(), "null keywords", null, 0L, Instant.now()), nullKeywords));
			given(pageResponseMapper.toCursorPageResponse(any(), any(), any(), anyLong()))
				.willAnswer(invocation -> {
					@SuppressWarnings("unchecked")
					Slice<ArticleDto> slice = invocation.getArgument(0);
					assertThat(slice).isSameAs(articleSlice);
					Number totalElements = invocation.getArgument(3, Number.class);
					return CursorPageResponse.<ArticleDto>builder()
						.content(slice.getContent())
						.nextCursor(invocation.getArgument(1, String.class))
						.nextAfter(invocation.getArgument(2, String.class))
						.size(slice.getNumberOfElements())
						.totalElements(totalElements.longValue())
						.hasNext(slice.hasNext())
						.build();
				});

			CursorPageResponse<ArticleDto> response = articleService.search(request);

			assertThat(response.content()).hasSize(1);
			assertThat(response.content().get(0)).isSameAs(original);
		}
	}

	@Nested
	class RegisterArticleView {
		@Test
		@DisplayName("기사 첫 뷰 등록 시 성공 지표가 증가한다")
		void registerArticleViewIncrementsSuccessMetric() {
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			Article article = Article.builder()
				.id(articleId)
				.source(ArticleSource.NAVER)
				.sourceUrl("https://news.example.com/article")
				.title("뉴스 제목")
				.publishDate(Instant.now())
				.collectedAt(Instant.now())
				.summary("요약")
				.commentCount(0L)
				.viewCount(0L)
				.build();
			User user = User.builder()
				.id(userId)
				.email("user@example.com")
				.nickname("tester")
				.password("secret")
				.build();

			given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(articleViewRepository.existsByUserIdAndArticleId(userId, articleId)).willReturn(false);
			given(articleViewMapper.toDto(any(ArticleView.class))).willReturn(new ArticleViewDto(
				UUID.randomUUID(),
				userId,
				Instant.now(),
				articleId,
				article.getSource().name(),
				article.getSourceUrl(),
				article.getTitle(),
				article.getPublishDate(),
				article.getSummary(),
				article.getCommentCount(),
				article.getViewCount()
			));

			articleService.registerArticleView(articleId, userId);

			Counter counter = counters.get("article.view.register.success");
			assertThat(counter).isNotNull();
			verify(counter).increment();
		}

		@Test
		@DisplayName("중복 뷰 등록 시 중복 지표가 증가한다")
		void duplicateViewRegistrationIncrementsDuplicateMetric() {
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			Article article = Article.builder()
				.id(articleId)
				.source(ArticleSource.NAVER)
				.sourceUrl("https://news.example.com/article")
				.title("뉴스 제목")
				.publishDate(Instant.now())
				.collectedAt(Instant.now())
				.summary("요약")
				.commentCount(0L)
				.viewCount(0L)
				.build();
			User user = User.builder()
				.id(userId)
				.email("user@example.com")
				.nickname("tester")
				.password("secret")
				.build();

			given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(articleViewRepository.existsByUserIdAndArticleId(userId, articleId)).willReturn(true);

			assertThatThrownBy(() -> articleService.registerArticleView(articleId, userId))
				.isInstanceOf(ArticleViewAlreadyExistException.class);

			Counter counter = counters.get("article.view.register.duplicate");
			assertThat(counter).isNotNull();
			verify(counter).increment();
			verify(articleViewRepository, never()).save(any());
		}
	}

}
