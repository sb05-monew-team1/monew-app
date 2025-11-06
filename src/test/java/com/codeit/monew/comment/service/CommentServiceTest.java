package com.codeit.monew.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.codeit.monew.activity.service.UserActivityService;
import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.domain.Comment;
import com.codeit.monew.comment.domain.CommentLike;
import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentRegisterRequest;
import com.codeit.monew.comment.dto.CommentSearchRequest;
import com.codeit.monew.comment.dto.CommentUpdateRequest;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.notification.service.NotificationService;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;
import com.querydsl.core.types.Order;

import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
	private NotificationService notificationService;

	@Mock
	private PageResponseMapper pageResponseMapper;

	@Mock
	private UserActivityService userActivityService;

	@InjectMocks
	private CommentService commentService;

	@Nested
	@DisplayName("댓글 등록 테스트")
	class RegisterCommentTest {

		@Test
		@DisplayName("댓글 등록 성공")
		void registerComment_Success() {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "테스트 댓글입니다");

			Article article = Article.builder().build();
			User user = User.builder().nickname("테스트유저").build();
			ReflectionTestUtils.setField(user, "id", userId);

			Comment savedComment = Comment.builder()
				.article(article)
				.user(user)
				.content(request.content())
				.likeCount(0L)
				.build();

			CommentDto expectedDto = new CommentDto(
				UUID.randomUUID(),
				articleId,
				userId,
				"테스트유저",
				"테스트 댓글입니다",
				0L,
				false,
				Instant.now()
			);

			when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
			when(commentMapper.toDto(any(Comment.class), anyString(), anyBoolean())).thenReturn(expectedDto);
			doNothing().when(userActivityService).deleteUserActivity(userId);

			// when
			CommentDto result = commentService.registerComment(request);

			// then
			assertNotNull(result);
			assertEquals(expectedDto.content(), result.content());
			assertEquals(expectedDto.userNickname(), result.userNickname());
			verify(articleRepository, times(1)).findById(articleId);
			verify(userRepository, times(1)).findById(userId);
			verify(commentRepository, times(1)).save(any(Comment.class));
			verify(userActivityService, times(1)).deleteUserActivity(userId);
		}

		@Test
		@DisplayName("기사 없음 - 예외 발생")
		void registerComment_Failure_ArticleNotFound() {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "테스트 댓글");

			when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.registerComment(request);
			});

			assertEquals(ErrorCode.ARTICLE_NOT_FOUND, exception.getErrorCode());
			verify(articleRepository, times(1)).findById(articleId);
			verify(userRepository, never()).findById(any());
			verify(commentRepository, never()).save(any());
		}

		@Test
		@DisplayName("사용자 없음 - 예외 발생")
		void registerComment_Failure_UserNotFound() {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "테스트 댓글");

			Article article = Article.builder().build();

			when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.registerComment(request);
			});

			assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
			verify(articleRepository, times(1)).findById(articleId);
			verify(userRepository, times(1)).findById(userId);
			verify(commentRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("댓글 수정 테스트")
	class UpdateCommentTest {

		@Test
		@DisplayName("댓글 수정 성공 - 본인 댓글")
		void updateComment_Success() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글 내용");

			User user = User.builder().nickname("작성자").build();
			ReflectionTestUtils.setField(user, "id", userId);

			Comment comment = Comment.builder()
				.user(user)
				.content("원본 댓글 내용")
				.likeCount(5L)
				.build();

			CommentDto expectedDto = new CommentDto(
				commentId,
				UUID.randomUUID(),
				userId,
				"작성자",
				"수정된 댓글 내용",
				5L,
				false,
				Instant.now()
			);

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			when(commentMapper.toDto(any(Comment.class), anyString(), anyBoolean())).thenReturn(expectedDto);
			doNothing().when(userActivityService).deleteUserActivity(userId);

			// when
			CommentDto result = commentService.updateComment(commentId, userId, request);

			// then
			assertNotNull(result);
			assertEquals(expectedDto.content(), result.content());
			verify(commentRepository, times(1)).findById(commentId);
			verify(userActivityService, times(1)).deleteUserActivity(userId);
		}

		@Test
		@DisplayName("댓글 없음 - 예외 발생")
		void updateComment_Failure_CommentNotFound() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentUpdateRequest request = new CommentUpdateRequest("수정 내용");

			when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.updateComment(commentId, userId, request);
			});

			assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
			verify(commentRepository, times(1)).findById(commentId);
		}

		@Test
		@DisplayName("권한 없음 - 다른 사용자의 댓글 수정 시도")
		void updateComment_Failure_Forbidden() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID commentOwnerId = UUID.randomUUID();
			UUID requestUserId = UUID.randomUUID();
			CommentUpdateRequest request = new CommentUpdateRequest("수정 시도");

			User commentOwner = User.builder().nickname("원작성자").build();
			ReflectionTestUtils.setField(commentOwner, "id", commentOwnerId);

			Comment comment = Comment.builder()
				.user(commentOwner)
				.content("원본 댓글")
				.likeCount(0L)
				.build();

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.updateComment(commentId, requestUserId, request);
			});

			assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
			verify(commentRepository, times(1)).findById(commentId);
		}
	}

	@Nested
	@DisplayName("댓글 논리 삭제 테스트")
	class SoftDeleteCommentTest {

		@Test
		@DisplayName("댓글 논리 삭제 성공")
		void softDeleteComment_Success() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			User user = User.builder().nickname("사용자").build();
			ReflectionTestUtils.setField(user, "id", userId);

			Comment comment = Comment.builder()
				.user(user)
				.content("삭제할 댓글")
				.likeCount(0L)
				.build();

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			doNothing().when(userActivityService).deleteUserActivity(userId);

			// when
			commentService.softDeleteComment(commentId);

			// then
			verify(commentRepository, times(1)).findById(commentId);
			verify(userActivityService, times(1)).deleteUserActivity(userId);
		}

		@Test
		@DisplayName("댓글 없음 - 예외 발생")
		void softDeleteComment_Failure_CommentNotFound() {
			// given
			UUID commentId = UUID.randomUUID();

			when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.softDeleteComment(commentId);
			});

			assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
			verify(commentRepository, times(1)).findById(commentId);
		}
	}

	@Nested
	@DisplayName("댓글 물리 삭제 테스트")
	class HardDeleteCommentTest {

		@Test
		@DisplayName("댓글 물리 삭제 성공")
		void hardDeleteComment_Success() {
			// given
			UUID commentId = UUID.randomUUID();
			Comment comment = Comment.builder()
				.content("삭제할 댓글")
				.likeCount(0L)
				.build();

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			doNothing().when(commentRepository).delete(comment);

			// when
			commentService.hardDeleteComment(commentId);

			// then
			verify(commentRepository, times(1)).findById(commentId);
			verify(commentRepository, times(1)).delete(comment);
		}

		@Test
		@DisplayName("댓글 없음 - 예외 발생")
		void hardDeleteComment_Failure_CommentNotFound() {
			// given
			UUID commentId = UUID.randomUUID();

			when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.hardDeleteComment(commentId);
			});

			assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
			verify(commentRepository, times(1)).findById(commentId);
			verify(commentRepository, never()).delete(any());
		}
	}

	@Nested
	@DisplayName("댓글 좋아요 등록 테스트")
	class LikeCommentTest {

		@Test
		@DisplayName("댓글 좋아요 등록 성공")
		void likeComment_Success() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			UUID commentOwnerId = UUID.randomUUID();
			User commentOwner = User.builder().nickname("댓글작성자").build();
			ReflectionTestUtils.setField(commentOwner, "id", commentOwnerId);

			User likeUser = User.builder().nickname("좋아요유저").build();
			ReflectionTestUtils.setField(likeUser, "id", userId);

			Comment comment = Comment.builder()
				.user(commentOwner)
				.content("댓글 내용")
				.likeCount(0L)
				.build();

			CommentLike commentLike = CommentLike.builder()
				.comment(comment)
				.user(likeUser)
				.build();

			CommentDto expectedDto = new CommentDto(
				commentId,
				UUID.randomUUID(),
				commentOwner.getId(),
				"댓글작성자",
				"댓글 내용",
				1L,
				true,
				Instant.now()
			);

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			when(userRepository.findById(userId)).thenReturn(Optional.of(likeUser));
			when(commentLikeRepository.existsByCommentAndUser(comment, likeUser)).thenReturn(false);
			when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(commentLike);
			when(commentMapper.toDto(any(Comment.class), anyString(), eq(true))).thenReturn(expectedDto);
			doNothing().when(notificationService).create(any());
			doNothing().when(userActivityService).deleteUserActivity(any(UUID.class));

			// when
			CommentDto result = commentService.likeComment(commentId, userId);

			// then
			assertNotNull(result);
			assertEquals(true, result.likedByMe());
			verify(commentRepository, times(1)).findById(commentId);
			verify(userRepository, times(1)).findById(userId);
			verify(commentLikeRepository, times(1)).existsByCommentAndUser(comment, likeUser);
			verify(commentLikeRepository, times(1)).save(any(CommentLike.class));
			verify(notificationService, times(1)).create(any());
			verify(userActivityService, times(1)).deleteUserActivity(userId);
		}

		@Test
		@DisplayName("댓글 없음 - 예외 발생")
		void likeComment_Failure_CommentNotFound() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.likeComment(commentId, userId);
			});

			assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
			verify(commentRepository, times(1)).findById(commentId);
			verify(userRepository, never()).findById(any());
		}

		@Test
		@DisplayName("사용자 없음 - 예외 발생")
		void likeComment_Failure_UserNotFound() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			Comment comment = Comment.builder()
				.content("댓글 내용")
				.likeCount(0L)
				.build();

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.likeComment(commentId, userId);
			});

			assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
			verify(commentRepository, times(1)).findById(commentId);
			verify(userRepository, times(1)).findById(userId);
			verify(commentLikeRepository, never()).save(any());
		}

		@Test
		@DisplayName("중복 좋아요 - 예외 발생")
		void likeComment_Failure_AlreadyLiked() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			User user = User.builder().nickname("유저").build();
			Comment comment = Comment.builder()
				.content("댓글")
				.likeCount(1L)
				.build();

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(commentLikeRepository.existsByCommentAndUser(comment, user)).thenReturn(true);

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.likeComment(commentId, userId);
			});

			assertEquals(ErrorCode.COMMENT_LIKE_ALREADY_EXISTS, exception.getErrorCode());
			verify(commentLikeRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("댓글 좋아요 취소 테스트")
	class UnlikeCommentTest {

		@Test
		@DisplayName("댓글 좋아요 취소 성공")
		void unlikeComment_Success() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			User user = User.builder().nickname("유저").build();
			ReflectionTestUtils.setField(user, "id", userId);

			Comment comment = Comment.builder()
				.user(user)
				.content("댓글")
				.likeCount(1L)
				.build();

			CommentLike commentLike = CommentLike.builder()
				.comment(comment)
				.user(user)
				.build();

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(commentLikeRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.of(commentLike));
			doNothing().when(commentLikeRepository).delete(commentLike);
			doNothing().when(userActivityService).deleteUserActivity(userId);

			// when
			commentService.unlikeComment(commentId, userId);

			// then
			verify(commentRepository, times(1)).findById(commentId);
			verify(userRepository, times(1)).findById(userId);
			verify(commentLikeRepository, times(1)).findByCommentAndUser(comment, user);
			verify(commentLikeRepository, times(1)).delete(commentLike);
			verify(userActivityService, times(1)).deleteUserActivity(userId);
		}

		@Test
		@DisplayName("댓글 없음 - 예외 발생")
		void unlikeComment_Failure_CommentNotFound() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.unlikeComment(commentId, userId);
			});

			assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
			verify(commentRepository, times(1)).findById(commentId);
			verify(userRepository, never()).findById(any());
		}

		@Test
		@DisplayName("사용자 없음 - 예외 발생")
		void unlikeComment_Failure_UserNotFound() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			Comment comment = Comment.builder()
				.content("댓글")
				.likeCount(1L)
				.build();

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.unlikeComment(commentId, userId);
			});

			assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
			verify(commentRepository, times(1)).findById(commentId);
			verify(userRepository, times(1)).findById(userId);
			verify(commentLikeRepository, never()).delete(any());
		}

		@Test
		@DisplayName("좋아요 기록 없음 - 예외 발생")
		void unlikeComment_Failure_LikeNotFound() {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			User user = User.builder().nickname("유저").build();
			Comment comment = Comment.builder()
				.content("댓글")
				.likeCount(0L)
				.build();

			when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(commentLikeRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.unlikeComment(commentId, userId);
			});

			assertEquals(ErrorCode.COMMENT_LIKE_NOT_FOUND, exception.getErrorCode());
			verify(commentLikeRepository, never()).delete(any());
		}
	}

	@Nested
	@DisplayName("댓글 목록 조회 테스트")
	class GetCommentsTest {

		@Test
		@DisplayName("댓글 목록 조회 성공 - 날짜순 내림차순")
		void getComments_Success_OrderByCreatedAtDesc() {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentSearchRequest request = new CommentSearchRequest(
				articleId,
				"createdAt",
				Order.DESC,
				null,
				null,
				10,
				userId
			);

			Article article = Article.builder().build();
			User user = User.builder().nickname("조회자").build();
			ReflectionTestUtils.setField(user, "id", userId);

			User commentAuthor1 = User.builder().nickname("작성자1").build();
			User commentAuthor2 = User.builder().nickname("작성자2").build();

			Comment comment1 = Comment.builder()
				.user(commentAuthor1)
				.content("댓글1")
				.likeCount(0L)
				.build();

			Comment comment2 = Comment.builder()
				.user(commentAuthor2)
				.content("댓글2")
				.likeCount(0L)
				.build();

			List<Comment> comments = List.of(comment1, comment2);
			Pageable pageable = PageRequest.of(0, 11);
			Slice<Comment> commentSlice = new SliceImpl<>(comments, pageable, false);

			CommentDto dto1 = new CommentDto(UUID.randomUUID(), articleId, userId, "작성자1", "댓글1", 0L, false, Instant.now());
			CommentDto dto2 = new CommentDto(UUID.randomUUID(), articleId, userId, "작성자2", "댓글2", 0L, false, Instant.now());

			CursorPageResponse<CommentDto> expectedResponse = CursorPageResponse.<CommentDto>builder()
				.content(List.of(dto1, dto2))
				.nextCursor(null)
				.nextAfter(null)
				.size(10)
				.totalElements(-1L)
				.hasNext(false)
				.build();

			when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
			when(commentRepository.findByArticleAndDeletedAtIsNullOrderByCreatedAtDesc(eq(article), any(Pageable.class)))
				.thenReturn(commentSlice);
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(commentLikeRepository.existsByCommentAndUser(any(Comment.class), eq(user))).thenReturn(false);
			when(commentMapper.toDto(any(Comment.class), anyString(), anyBoolean()))
				.thenReturn(dto1, dto2);
			when(pageResponseMapper.toCursorPageResponse(any(Slice.class), any(), any(), eq(-1L)))
				.thenReturn(expectedResponse);

			// when
			CursorPageResponse<CommentDto> result = commentService.getComments(request);

			// then
			assertNotNull(result);
			assertEquals(2, result.content().size());
			assertFalse(result.hasNext());
			verify(articleRepository, times(1)).findById(articleId);
			verify(userRepository, times(1)).findById(userId);
		}

		@Test
		@DisplayName("댓글 목록 조회 성공 - 좋아요순 내림차순")
		void getComments_Success_OrderByLikeCountDesc() {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentSearchRequest request = new CommentSearchRequest(
				articleId,
				"likeCount",
				Order.DESC,
				null,
				null,
				10,
				userId
			);

			Article article = Article.builder().build();
			User user = User.builder().nickname("조회자").build();
			ReflectionTestUtils.setField(user, "id", userId);

			User commentAuthor = User.builder().nickname("작성자").build();

			Comment comment = Comment.builder()
				.user(commentAuthor)
				.content("인기 댓글")
				.likeCount(10L)
				.build();

			List<Comment> comments = List.of(comment);
			Pageable pageable = PageRequest.of(0, 11);
			Slice<Comment> commentSlice = new SliceImpl<>(comments, pageable, false);

			CommentDto dto = new CommentDto(UUID.randomUUID(), articleId, userId, "작성자", "인기 댓글", 10L, false, Instant.now());

			CursorPageResponse<CommentDto> expectedResponse = CursorPageResponse.<CommentDto>builder()
				.content(List.of(dto))
				.nextCursor(null)
				.nextAfter(null)
				.size(10)
				.totalElements(-1L)
				.hasNext(false)
				.build();

			when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
			when(commentRepository.findByArticleAndNotDeletedOrderByLikesDesc(eq(article), isNull(), isNull(), any(Pageable.class)))
				.thenReturn(commentSlice);
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(commentLikeRepository.existsByCommentAndUser(any(Comment.class), eq(user))).thenReturn(false);
			when(commentMapper.toDto(any(Comment.class), anyString(), anyBoolean())).thenReturn(dto);
			when(pageResponseMapper.toCursorPageResponse(any(Slice.class), any(), any(), eq(-1L)))
				.thenReturn(expectedResponse);

			// when
			CursorPageResponse<CommentDto> result = commentService.getComments(request);

			// then
			assertNotNull(result);
			assertEquals(1, result.content().size());
			verify(articleRepository, times(1)).findById(articleId);
		}

		@Test
		@DisplayName("기사 없음 - 예외 발생")
		void getComments_Failure_ArticleNotFound() {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentSearchRequest request = new CommentSearchRequest(
				articleId,
				"createdAt",
				Order.DESC,
				null,
				null,
				10,
				userId
			);

			when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.getComments(request);
			});

			assertEquals(ErrorCode.ARTICLE_NOT_FOUND, exception.getErrorCode());
			verify(articleRepository, times(1)).findById(articleId);
			verify(commentRepository, never()).findByArticleAndDeletedAtIsNullOrderByCreatedAtDesc(any(), any());
		}

		@Test
		@DisplayName("조회 사용자 없음 - 예외 발생")
		void getComments_Failure_UserNotFound() {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentSearchRequest request = new CommentSearchRequest(
				articleId,
				"createdAt",
				Order.DESC,
				null,
				null,
				10,
				userId
			);

			Article article = Article.builder().build();
			Comment comment = Comment.builder().content("댓글").likeCount(0L).build();
			List<Comment> comments = List.of(comment);
			Pageable pageable = PageRequest.of(0, 11);
			Slice<Comment> commentSlice = new SliceImpl<>(comments, pageable, false);

			when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
			when(commentRepository.findByArticleAndDeletedAtIsNullOrderByCreatedAtDesc(eq(article), any(Pageable.class)))
				.thenReturn(commentSlice);
			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				commentService.getComments(request);
			});

			assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
			verify(userRepository, times(1)).findById(userId);
		}
	}
}
