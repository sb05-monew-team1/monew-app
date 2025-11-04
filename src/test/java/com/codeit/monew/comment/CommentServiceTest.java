package com.codeit.monew.comment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentSearchRequest;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.service.CommentService;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;
import com.querydsl.core.types.Order;

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

	@InjectMocks
	private CommentService commentService;

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
				return CursorPageResponse.<CommentDto>builder()
					.content(slice.getContent())
					.nextCursor(invocation.getArgument(1))
					.nextAfter(invocation.getArgument(2))
					.size(slice.getNumberOfElements())
					.totalElements(invocation.getArgument(3))
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
				return CursorPageResponse.<CommentDto>builder()
					.content(slice.getContent())
					.nextCursor(invocation.getArgument(1))
					.nextAfter(invocation.getArgument(2))
					.size(slice.getNumberOfElements())
					.totalElements(invocation.getArgument(3))
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
