package com.codeit.monew.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentRegisterRequest;
import com.codeit.monew.comment.dto.CommentSearchRequest;
import com.codeit.monew.comment.dto.CommentUpdateRequest;
import com.codeit.monew.comment.service.CommentService;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CommentController 테스트
 */
@WebMvcTest(controllers = CommentController.class,
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableJpaAuditing.class)
	})
@WithMockUser
class CommentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CommentService commentService;

	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Nested
	@DisplayName("댓글 등록 API 테스트")
	class RegisterCommentTest {

		@Test
		@DisplayName("댓글 등록 성공 - 201 Created")
		void registerComment_Success() throws Exception {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "테스트 댓글");

			CommentDto expectedDto = new CommentDto(
				UUID.randomUUID(),
				articleId,
				userId,
				"테스트유저",
				"테스트 댓글",
				0L,
				false,
				Instant.now()
			);

			when(commentService.registerComment(any(CommentRegisterRequest.class))).thenReturn(expectedDto);

			// when & then
			mockMvc.perform(post("/api/comments")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(expectedDto.id().toString()))
				.andExpect(jsonPath("$.content").value("테스트 댓글"))
				.andExpect(jsonPath("$.userNickname").value("테스트유저"));

			verify(commentService, times(1)).registerComment(any(CommentRegisterRequest.class));
		}

		@Test
		@DisplayName("기사 없음 - 404 Not Found")
		void registerComment_Failure_ArticleNotFound() throws Exception {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "테스트 댓글");

			when(commentService.registerComment(any(CommentRegisterRequest.class)))
				.thenThrow(new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/comments")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).registerComment(any(CommentRegisterRequest.class));
		}

		@Test
		@DisplayName("사용자 없음 - 404 Not Found")
		void registerComment_Failure_UserNotFound() throws Exception {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "테스트 댓글");

			when(commentService.registerComment(any(CommentRegisterRequest.class)))
				.thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/comments")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).registerComment(any(CommentRegisterRequest.class));
		}
	}

	@Nested
	@DisplayName("댓글 수정 API 테스트")
	class UpdateCommentTest {

		@Test
		@DisplayName("댓글 수정 성공 - 200 OK")
		void updateComment_Success() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

			CommentDto expectedDto = new CommentDto(
				commentId,
				UUID.randomUUID(),
				userId,
				"유저",
				"수정된 댓글",
				5L,
				false,
				Instant.now()
			);

			when(commentService.updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class)))
				.thenReturn(expectedDto);

			// when & then
			mockMvc.perform(patch("/api/comments/{commentId}", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(commentId.toString()))
				.andExpect(jsonPath("$.content").value("수정된 댓글"));

			verify(commentService, times(1)).updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class));
		}

		@Test
		@DisplayName("댓글 없음 - 404 Not Found")
		void updateComment_Failure_CommentNotFound() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

			when(commentService.updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class)))
				.thenThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

			// when & then
			mockMvc.perform(patch("/api/comments/{commentId}", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class));
		}

		@Test
		@DisplayName("권한 없음 - 403 Forbidden")
		void updateComment_Failure_Forbidden() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();
			CommentUpdateRequest request = new CommentUpdateRequest("수정 시도");

			when(commentService.updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class)))
				.thenThrow(new BusinessException(ErrorCode.FORBIDDEN));

			// when & then
			mockMvc.perform(patch("/api/comments/{commentId}", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());

			verify(commentService, times(1)).updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class));
		}
	}

	@Nested
	@DisplayName("댓글 논리 삭제 API 테스트")
	class SoftDeleteCommentTest {

		@Test
		@DisplayName("댓글 논리 삭제 성공 - 204 No Content")
		void softDeleteComment_Success() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();

			doNothing().when(commentService).softDeleteComment(commentId);

			// when & then
			mockMvc.perform(delete("/api/comments/{commentId}", commentId)
				.with(csrf()))
				.andExpect(status().isNoContent());

			verify(commentService, times(1)).softDeleteComment(commentId);
		}

		@Test
		@DisplayName("댓글 없음 - 404 Not Found")
		void softDeleteComment_Failure_CommentNotFound() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();

			doThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND))
				.when(commentService).softDeleteComment(commentId);

			// when & then
			mockMvc.perform(delete("/api/comments/{commentId}", commentId)
				.with(csrf()))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).softDeleteComment(commentId);
		}
	}

	@Nested
	@DisplayName("댓글 물리 삭제 API 테스트")
	class HardDeleteCommentTest {

		@Test
		@DisplayName("댓글 물리 삭제 성공 - 204 No Content")
		void hardDeleteComment_Success() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();

			doNothing().when(commentService).hardDeleteComment(commentId);

			// when & then
			mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
				.with(csrf()))
				.andExpect(status().isNoContent());

			verify(commentService, times(1)).hardDeleteComment(commentId);
		}

		@Test
		@DisplayName("댓글 없음 - 404 Not Found")
		void hardDeleteComment_Failure_CommentNotFound() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();

			doThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND))
				.when(commentService).hardDeleteComment(commentId);

			// when & then
			mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
				.with(csrf()))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).hardDeleteComment(commentId);
		}
	}

	@Nested
	@DisplayName("댓글 좋아요 등록 API 테스트")
	class LikeCommentTest {

		@Test
		@DisplayName("좋아요 등록 성공 - 200 OK")
		void likeComment_Success() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			CommentDto expectedDto = new CommentDto(
				commentId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				"댓글작성자",
				"댓글 내용",
				1L,
				true,
				Instant.now()
			);

			when(commentService.likeComment(commentId, userId)).thenReturn(expectedDto);

			// when & then
			mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(commentId.toString()))
				.andExpect(jsonPath("$.likedByMe").value(true))
				.andExpect(jsonPath("$.likeCount").value(1));

			verify(commentService, times(1)).likeComment(commentId, userId);
		}

		@Test
		@DisplayName("댓글 없음 - 404 Not Found")
		void likeComment_Failure_CommentNotFound() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			when(commentService.likeComment(commentId, userId))
				.thenThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString()))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).likeComment(commentId, userId);
		}

		@Test
		@DisplayName("중복 좋아요 - 409 Conflict")
		void likeComment_Failure_AlreadyLiked() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			when(commentService.likeComment(commentId, userId))
				.thenThrow(new BusinessException(ErrorCode.COMMENT_LIKE_ALREADY_EXISTS));

			// when & then
			mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString()))
				.andExpect(status().isConflict());

			verify(commentService, times(1)).likeComment(commentId, userId);
		}
	}

	@Nested
	@DisplayName("댓글 좋아요 취소 API 테스트")
	class UnlikeCommentTest {

		@Test
		@DisplayName("좋아요 취소 성공 - 200 OK")
		void unlikeComment_Success() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			doNothing().when(commentService).unlikeComment(commentId, userId);

			// when & then
			mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString()))
				.andExpect(status().isOk());

			verify(commentService, times(1)).unlikeComment(commentId, userId);
		}

		@Test
		@DisplayName("댓글 없음 - 404 Not Found")
		void unlikeComment_Failure_CommentNotFound() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			doThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND))
				.when(commentService).unlikeComment(commentId, userId);

			// when & then
			mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString()))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).unlikeComment(commentId, userId);
		}

		@Test
		@DisplayName("좋아요 없음 - 404 Not Found")
		void unlikeComment_Failure_LikeNotFound() throws Exception {
			// given
			UUID commentId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			doThrow(new BusinessException(ErrorCode.COMMENT_LIKE_NOT_FOUND))
				.when(commentService).unlikeComment(commentId, userId);

			// when & then
			mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
					.with(csrf())
					.header("Monew-Request-User-ID", userId.toString()))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).unlikeComment(commentId, userId);
		}
	}

	@Nested
	@DisplayName("댓글 목록 조회 API 테스트")
	class GetCommentsTest {

		@Test
		@DisplayName("댓글 목록 조회 성공 - 200 OK")
		void getComments_Success() throws Exception {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			CommentDto comment1 = new CommentDto(
				UUID.randomUUID(),
				articleId,
				UUID.randomUUID(),
				"유저1",
				"댓글1",
				5L,
				false,
				Instant.now()
			);

			CommentDto comment2 = new CommentDto(
				UUID.randomUUID(),
				articleId,
				UUID.randomUUID(),
				"유저2",
				"댓글2",
				3L,
				true,
				Instant.now()
			);

			CursorPageResponse<CommentDto> response = CursorPageResponse.<CommentDto>builder()
				.content(List.of(comment1, comment2))
				.nextCursor("next-cursor")
				.nextAfter(null)
				.size(2)
				.totalElements(2L)
				.hasNext(true)
				.build();

			when(commentService.getComments(any(CommentSearchRequest.class))).thenReturn(response);

			// when & then
			mockMvc.perform(get("/api/comments")
					.param("articleId", articleId.toString())
					.param("orderBy", "createdAt")
					.param("direction", "DESC")
					.param("limit", "10")
					.header("Monew-Request-User-ID", userId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.content[0].content").value("댓글1"))
				.andExpect(jsonPath("$.content[1].content").value("댓글2"))
				.andExpect(jsonPath("$.nextCursor").value("next-cursor"))
				.andExpect(jsonPath("$.hasNext").value(true));

			verify(commentService, times(1)).getComments(any(CommentSearchRequest.class));
		}

		@Test
		@DisplayName("기사 없음 - 404 Not Found")
		void getComments_Failure_ArticleNotFound() throws Exception {
			// given
			UUID articleId = UUID.randomUUID();
			UUID userId = UUID.randomUUID();

			when(commentService.getComments(any(CommentSearchRequest.class)))
				.thenThrow(new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

			// when & then
			mockMvc.perform(get("/api/comments")
					.param("articleId", articleId.toString())
					.param("orderBy", "createdAt")
					.param("direction", "DESC")
					.param("limit", "10")
					.header("Monew-Request-User-ID", userId.toString()))
				.andExpect(status().isNotFound());

			verify(commentService, times(1)).getComments(any(CommentSearchRequest.class));
		}
	}
}
