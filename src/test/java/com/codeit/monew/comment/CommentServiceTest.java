package com.codeit.monew.comment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.domain.ArticleSource;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.domain.Comment;
import com.codeit.monew.comment.domain.CommentLike;
import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentRegisterRequest;
import com.codeit.monew.comment.dto.CommentSearchRequest;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.service.CommentService;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.notification.dto.NotificationCreateRequest;
import com.codeit.monew.notification.service.NotificationService;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;
import com.querydsl.core.types.Order;

import com.codeit.monew.activity.service.UserActivityService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

	@Mock
	private CommentRepository commentRepository;
	@Mock
	private CommentLikeRepository commentLikeRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ArticleRepository articleRepository;
	@Mock
	private CommentMapper commentMapper;
	@Mock
	private PageResponseMapper pageResponseMapper;
	@Mock
	private NotificationService notificationService;
	@Mock
	private UserActivityService userActivityService;
	@Mock
	private MeterRegistry meterRegistry;

	@InjectMocks
	private CommentService commentService;

	private final Map<String, Counter> counters = new HashMap<>();

	@BeforeEach
	void setUpMeterRegistry() {
		counters.clear();
		Mockito.lenient().when(meterRegistry.counter(anyString()))
			.thenAnswer(invocation -> counters.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(Counter.class)));
		Mockito.lenient().when(meterRegistry.counter(anyString(), any(String[].class)))
			.thenAnswer(invocation -> counters.computeIfAbsent(
				invocation.getArgument(0, String.class), key -> Mockito.mock(Counter.class)));
	}
	@Test
	@DisplayName("좋아요순 댓글 조회 시 복합 커서를 생성한다")
	void getCommentsLikeOrderBuildsCompositeCursor() {
		UUID articleId = UUID.randomUUID();
		UUID requestUserId = UUID.randomUUID();
		CommentSearchRequest request = new CommentSearchRequest(
			articleId,
			"likeCount",
			Order.DESC,
			null,
			null,
			1,
			requestUserId
		);

		Article article = buildArticle(articleId);
		User requestUser = buildUser(requestUserId, "viewer");
		User commentAuthor = buildUser(UUID.randomUUID(), "author");
		Instant createdAt = Instant.parse("2024-06-01T10:15:30Z");
		long likeCount = 5L;

		Comment comment = Comment.builder()
			.id(UUID.randomUUID())
			.article(article)
			.user(commentAuthor)
			.content("content")
			.likeCount(likeCount)
			.createdAt(createdAt)
			.build();

		Slice<Comment> commentSlice = new SliceImpl<>(
			List.of(comment),
			Pageable.ofSize(request.limit() + 1),
			true
		);

		CommentDto dto = new CommentDto(
			comment.getId(),
			articleId,
			commentAuthor.getId(),
			commentAuthor.getNickname(),
			comment.getContent(),
			likeCount,
			true,
			createdAt
		);

		given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
		given(commentRepository.findByArticleAndNotDeletedOrderByLikesDesc(
			eq(article), isNull(), isNull(), any(Pageable.class))).willReturn(commentSlice);
		given(userRepository.findById(requestUserId)).willReturn(Optional.of(requestUser));
		given(commentLikeRepository.existsByCommentAndUser(comment, requestUser)).willReturn(true);
		given(commentMapper.toDto(comment, "author", true)).willReturn(dto);
		given(pageResponseMapper.toCursorPageResponse(any(), any(), any(), anyLong()))
			.willAnswer(invocation -> {
				Slice<CommentDto> slice = invocation.getArgument(0);
				Number totalElements = invocation.getArgument(3, Number.class);
				return CursorPageResponse.<CommentDto>builder()
					.content(slice.getContent())
					.nextCursor(invocation.getArgument(1, String.class))
					.nextAfter(invocation.getArgument(2, String.class))
					.size(slice.getNumberOfElements())
					.totalElements(totalElements.longValue())
					.hasNext(slice.hasNext())
					.build();
			});

		CursorPageResponse<CommentDto> response = commentService.getComments(request);

		assertThat(response.nextCursor()).isEqualTo(likeCount + "_" + createdAt);
		assertThat(response.nextAfter()).isEqualTo(createdAt.toString());
		assertThat(response.hasNext()).isTrue();
		verify(commentRepository).findByArticleAndNotDeletedOrderByLikesDesc(
			article, null, null, PageRequest.of(0, request.limit() + 1));
	}

	@Test
	@DisplayName("날짜순 댓글 조회 시 createdAt 커서를 사용한다")
	void getCommentsDateOrderUsesCreatedAtCursor() {
		UUID articleId = UUID.randomUUID();
		UUID requestUserId = UUID.randomUUID();
		Instant cursorInstant = Instant.parse("2024-06-02T10:15:30Z");
		CommentSearchRequest request = new CommentSearchRequest(
			articleId,
			"createdAt",
			Order.ASC,
			cursorInstant.toString(),
			null,
			2,
			requestUserId
		);

		Article article = buildArticle(articleId);
		User requestUser = buildUser(requestUserId, "viewer");
		User commentAuthor = buildUser(UUID.randomUUID(), "author");
		Instant createdAt = cursorInstant.plusSeconds(60);

		Comment comment = Comment.builder()
			.id(UUID.randomUUID())
			.article(article)
			.user(commentAuthor)
			.content("new comment")
			.likeCount(0L)
			.createdAt(createdAt)
			.build();

		Slice<Comment> commentSlice = new SliceImpl<>(
			List.of(comment),
			Pageable.ofSize(request.limit() + 1),
			false
		);

		CommentDto dto = new CommentDto(
			comment.getId(),
			articleId,
			commentAuthor.getId(),
			commentAuthor.getNickname(),
			comment.getContent(),
			0L,
			false,
			createdAt
		);

		given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
		given(commentRepository.findByArticleAndDeletedAtIsNullAndCreatedAtGreaterThanOrderByCreatedAtAsc(
			eq(article), eq(cursorInstant), any(Pageable.class))).willReturn(commentSlice);
		given(userRepository.findById(requestUserId)).willReturn(Optional.of(requestUser));
		given(commentLikeRepository.existsByCommentAndUser(comment, requestUser)).willReturn(false);
		given(commentMapper.toDto(comment, "author", false)).willReturn(dto);
		given(pageResponseMapper.toCursorPageResponse(any(), any(), any(), anyLong()))
			.willAnswer(invocation -> {
				Slice<CommentDto> slice = invocation.getArgument(0);
				Number totalElements = invocation.getArgument(3, Number.class);
				return CursorPageResponse.<CommentDto>builder()
					.content(slice.getContent())
					.nextCursor(invocation.getArgument(1, String.class))
					.nextAfter(invocation.getArgument(2, String.class))
					.size(slice.getNumberOfElements())
					.totalElements(totalElements.longValue())
					.hasNext(slice.hasNext())
					.build();
			});

		CursorPageResponse<CommentDto> response = commentService.getComments(request);

		assertThat(response.nextCursor()).isNull();
		assertThat(response.nextAfter()).isNull();
		assertThat(response.hasNext()).isFalse();
		verify(commentRepository).findByArticleAndDeletedAtIsNullAndCreatedAtGreaterThanOrderByCreatedAtAsc(
			article, cursorInstant, PageRequest.of(0, request.limit() + 1));
	}

	@Test
	@DisplayName("날짜순 내림차순 조회 시 최신 순서로 커서를 생성한다")
	@SuppressWarnings("unchecked")
	void getCommentsDateOrderDescWithoutCursor() {
		UUID articleId = UUID.randomUUID();
		UUID requestUserId = UUID.randomUUID();
		CommentSearchRequest request = new CommentSearchRequest(
			articleId,
			"createdAt",
			Order.DESC,
			null,
			null,
			2,
			requestUserId
		);

		Article article = buildArticle(articleId);
		User requestUser = buildUser(requestUserId, "viewer");
		User commentAuthor = buildUser(UUID.randomUUID(), "author");
		Instant firstCreated = Instant.parse("2024-06-02T11:30:00Z");
		Instant secondCreated = Instant.parse("2024-06-02T11:20:00Z");

		Comment firstComment = Comment.builder()
			.id(UUID.randomUUID())
			.article(article)
			.user(commentAuthor)
			.content("latest")
			.likeCount(1L)
			.createdAt(firstCreated)
			.build();

		Comment secondComment = Comment.builder()
			.id(UUID.randomUUID())
			.article(article)
			.user(commentAuthor)
			.content("older")
			.likeCount(0L)
			.createdAt(secondCreated)
			.build();

		Slice<Comment> commentSlice = new SliceImpl<>(
			List.of(firstComment, secondComment),
			Pageable.ofSize(request.limit() + 1),
			true
		);

		CommentDto firstDto = new CommentDto(
			firstComment.getId(),
			articleId,
			commentAuthor.getId(),
			commentAuthor.getNickname(),
			firstComment.getContent(),
			1L,
			false,
			firstCreated
		);

		CommentDto secondDto = new CommentDto(
			secondComment.getId(),
			articleId,
			commentAuthor.getId(),
			commentAuthor.getNickname(),
			secondComment.getContent(),
			0L,
			false,
			secondCreated
		);

		given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
		given(commentRepository.findByArticleAndDeletedAtIsNullOrderByCreatedAtDesc(
			eq(article), any(Pageable.class))).willReturn(commentSlice);
		given(userRepository.findById(requestUserId)).willReturn(Optional.of(requestUser));
		given(commentLikeRepository.existsByCommentAndUser(firstComment, requestUser)).willReturn(false);
		given(commentLikeRepository.existsByCommentAndUser(secondComment, requestUser)).willReturn(false);
		given(commentMapper.toDto(firstComment, "author", false)).willReturn(firstDto);
		given(commentMapper.toDto(secondComment, "author", false)).willReturn(secondDto);
		given(pageResponseMapper.toCursorPageResponse(any(), any(), any(), anyLong()))
			.willAnswer(invocation -> {
				Slice<CommentDto> slice = invocation.getArgument(0);
				assertThat(invocation.getArgument(1, String.class)).isEqualTo(secondCreated.toString());
				assertThat(invocation.getArgument(2, String.class)).isEqualTo(secondCreated.toString());
				Number totalElements = invocation.getArgument(3, Number.class);
				return CursorPageResponse.<CommentDto>builder()
					.content(slice.getContent())
					.nextCursor(invocation.getArgument(1, String.class))
					.nextAfter(invocation.getArgument(2, String.class))
					.size(slice.getNumberOfElements())
					.totalElements(totalElements.longValue())
					.hasNext(slice.hasNext())
					.build();
			});

		CursorPageResponse<CommentDto> response = commentService.getComments(request);

		assertThat(response.nextCursor()).isEqualTo(secondCreated.toString());
		assertThat(response.nextAfter()).isEqualTo(secondCreated.toString());
		verify(commentRepository).findByArticleAndDeletedAtIsNullOrderByCreatedAtDesc(
			article, PageRequest.of(0, request.limit() + 1));
	}

	@Test
	@DisplayName("조회 결과가 없으면 커서를 반환하지 않는다")
	@SuppressWarnings("unchecked")
	void getCommentsReturnsEmptyCursorWhenSliceEmpty() {
		UUID articleId = UUID.randomUUID();
		UUID requestUserId = UUID.randomUUID();
		String cursorValue = "1_2024-06-01T00:00:00Z";
		Instant likeCursorCreatedAt = Instant.parse("2024-06-01T00:00:00Z");
		CommentSearchRequest request = new CommentSearchRequest(
			articleId,
			"likeCount",
			Order.ASC,
			cursorValue,
			null,
			3,
			requestUserId
		);

		Article article = buildArticle(articleId);
		User requestUser = buildUser(requestUserId, "viewer");

		Slice<Comment> emptySlice = new SliceImpl<>(
			List.of(),
			Pageable.ofSize(request.limit() + 1),
			false
		);

		given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
		given(commentRepository.findByArticleAndNotDeletedOrderByLikesAsc(
			eq(article), any(), any(), any(Pageable.class))).willReturn(emptySlice);
		given(userRepository.findById(requestUserId)).willReturn(Optional.of(requestUser));
		given(pageResponseMapper.toCursorPageResponse(any(), any(), any(), anyLong()))
			.willAnswer(invocation -> {
				assertThat(invocation.getArgument(1, String.class)).isNull();
				assertThat(invocation.getArgument(2, String.class)).isNull();
				Number totalElements = invocation.getArgument(3, Number.class);
				return CursorPageResponse.<CommentDto>builder()
					.content(List.of())
					.nextCursor(null)
					.nextAfter(null)
					.size(0)
					.totalElements(totalElements.longValue())
					.hasNext(false)
					.build();
			});

		CursorPageResponse<CommentDto> response = commentService.getComments(request);

		assertThat(response.nextCursor()).isNull();
		assertThat(response.nextAfter()).isNull();
		assertThat(response.hasNext()).isFalse();
		verify(commentRepository).findByArticleAndNotDeletedOrderByLikesAsc(
			article, 1L, likeCursorCreatedAt, PageRequest.of(0, request.limit() + 1));
	}

	@Test
	@DisplayName("댓글 등록 시 성공 지표를 증가시킨다")
	void registerCommentIncrementsMetric() {
		UUID articleId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "내용");

		Article article = buildArticle(articleId);
		User user = buildUser(userId, "author");
		UUID commentId = UUID.randomUUID();
		Comment savedComment = Comment.builder()
			.id(commentId)
			.article(article)
			.user(user)
			.content(request.content())
			.likeCount(0L)
			.build();

		given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(commentRepository.save(any(Comment.class))).willReturn(savedComment);
		given(commentMapper.toDto(savedComment, user.getNickname(), false)).willReturn(new CommentDto(
			commentId,
			articleId,
			userId,
			user.getNickname(),
			request.content(),
			0L,
			false,
			Instant.now()
		));

		commentService.registerComment(request);

		Counter counter = counters.get("comment.create.success");
		assertThat(counter).isNotNull();
		verify(counter).increment();
		verify(userActivityService).deleteUserActivity(userId);
	}

	@Test
	@DisplayName("댓글 좋아요 등록 시 성공 지표를 증가시킨다")
	void likeCommentIncrementsSuccessMetric() {
		UUID commentId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Article article = buildArticle(UUID.randomUUID());
		User author = buildUser(UUID.randomUUID(), "author");
		User liker = buildUser(userId, "liker");
		Comment comment = Comment.builder()
			.id(commentId)
			.article(article)
			.user(author)
			.content("test")
			.likeCount(0L)
			.build();

		given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
		given(userRepository.findById(userId)).willReturn(Optional.of(liker));
		given(commentLikeRepository.existsByCommentAndUser(comment, liker)).willReturn(false);
		given(commentLikeRepository.save(any(CommentLike.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(commentMapper.toDto(comment, author.getNickname(), true)).willReturn(new CommentDto(
			commentId,
			article.getId(),
			author.getId(),
			author.getNickname(),
			"test",
			1L,
			true,
			Instant.now()
		));
		given(notificationService.create(any(NotificationCreateRequest.class))).willAnswer(invocation -> null);

		commentService.likeComment(commentId, userId);

		Counter counter = counters.get("comment.like.success");
		assertThat(counter).isNotNull();
		verify(counter).increment();
		verify(notificationService).create(any(NotificationCreateRequest.class));
		verify(userActivityService).deleteUserActivity(userId);
	}

	@Test
	@DisplayName("이미 좋아요된 댓글이면 중복 지표를 증가시킨다")
	void likeCommentDuplicateIncrementsMetric() {
		UUID commentId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Article article = buildArticle(UUID.randomUUID());
		User author = buildUser(UUID.randomUUID(), "author");
		User liker = buildUser(userId, "liker");
		Comment comment = Comment.builder()
			.id(commentId)
			.article(article)
			.user(author)
			.content("test")
			.likeCount(1L)
			.build();

		given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
		given(userRepository.findById(userId)).willReturn(Optional.of(liker));
		given(commentLikeRepository.existsByCommentAndUser(comment, liker)).willReturn(true);

		assertThatThrownBy(() -> commentService.likeComment(commentId, userId))
			.isInstanceOf(BusinessException.class);

		Counter counter = counters.get("comment.like.duplicate");
		assertThat(counter).isNotNull();
		verify(counter).increment();
		verify(commentLikeRepository, never()).save(any());
	}

	private Article buildArticle(UUID articleId) {
		Instant now = Instant.now();
		return Article.builder()
			.id(articleId)
			.source(ArticleSource.NAVER)
			.sourceUrl("http://example.com/" + articleId)
			.title("title")
			.publishDate(now)
			.collectedAt(now)
			.summary("summary")
			.commentCount(0L)
			.viewCount(0L)
			.createdAt(now)
			.updatedAt(now)
			.build();
	}

	private User buildUser(UUID userId, String nickname) {
		return User.builder()
			.id(userId)
			.email(nickname + "@example.com")
			.nickname(nickname)
			.password("encrypted")
			.build();
	}
}
