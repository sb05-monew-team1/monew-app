package com.codeit.monew.comment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentLikeRequest;
import com.codeit.monew.comment.dto.CommentRegisterRequest;
import com.codeit.monew.comment.dto.CommentUpdateRequest;
import com.codeit.monew.comment.service.CommentService;
import com.codeit.monew.common.dto.CursorPageResponse;

/**
 * 댓글 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	/**
	 * 댓글 등록
	 * @param request 댓글 등록 요청
	 * @return 등록된 댓글 정보
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CommentDto registerComment(@Validated @RequestBody CommentRegisterRequest request) {
		log.info("댓글 등록 API 호출 - articleId: {}, userId: {}", request.articleId(), request.userId());
		return commentService.registerComment(request);
	}

	/**
	 * 댓글 수정
	 * @param commentId 댓글 ID
	 * @param requestUserId 요청자 ID (헤더)
	 * @param request 댓글 수정 요청
	 * @return 수정된 댓글 정보
	 */
	@PatchMapping("/{commentId}")
	public CommentDto updateComment(
		@PathVariable UUID commentId,
		@RequestHeader("Monew-Request-User-ID") UUID requestUserId,
		@Validated @RequestBody CommentUpdateRequest request
	) {
		log.info("댓글 수정 API 호출 - commentId: {}, requestUserId: {}", commentId, requestUserId);
		return commentService.updateComment(commentId, requestUserId, request);
	}

	/**
	 * 댓글 논리 삭제
	 * @param commentId 댓글 ID
	 */
	//수정완
	@DeleteMapping("/{commentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void softDeleteComment(@PathVariable UUID commentId) {
		log.info("댓글 논리 삭제 API 호출 - commentId: {}", commentId);
		commentService.softDeleteComment(commentId);
	}

	/**
	 * 댓글 물리 삭제 (테스트용)
	 * @param commentId 댓글 ID
	 */
	@DeleteMapping("/{commentId}/hard")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void hardDeleteComment(@PathVariable UUID commentId) {
		log.info("댓글 물리 삭제 API 호출 - commentId: {}", commentId);
		commentService.hardDeleteComment(commentId);
	}

	/**
	 * 댓글 좋아요 등록
	 * @param commentId 댓글 ID
	 * @param request 좋아요 등록 요청 (userId 포함)
	 */
	@PostMapping("/{commentId}/comment-likes")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void likeComment(
		@PathVariable UUID commentId,
		@Validated @RequestBody CommentLikeRequest request
	) {
		log.info("댓글 좋아요 등록 API 호출 - commentId: {}, userId: {}", commentId, request.userId());
		commentService.likeComment(commentId, request.userId());
	}

	/**
	 * 댓글 좋아요 취소
	 * @param commentId 댓글 ID
	 * @param request 좋아요 취소 요청 (userId 포함)
	 */
	@DeleteMapping("/{commentId}/comment-likes")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void unlikeComment(
		@PathVariable UUID commentId,
		@Validated @RequestBody CommentLikeRequest request
	) {
		log.info("댓글 좋아요 취소 API 호출 - commentId: {}, userId: {}", commentId, request.userId());
		commentService.unlikeComment(commentId, request.userId());
	}

	/**
	 * 특정 기사의 댓글 목록 조회 (정렬 및 커서 페이지네이션)
	 * @param articleId 기사 ID
	 * @param requestUserId 요청자 ID (선택적, 헤더)
	 * @param sortBy 정렬 조건 (선택적, "date" 또는 "likes", 기본값 "date")
	 * @param cursor 커서 (선택적, 다음 페이지 조회용)
	 * @param limit 조회할 댓글 개수 (선택적, 기본값 10)
	 * @return 댓글 목록 (커서 페이지네이션 응답)
	 */
	@GetMapping
	public CursorPageResponse<CommentDto> getComments(
		@RequestParam UUID articleId,
		@RequestHeader(value = "Monew-Request-User-ID", required = false) UUID requestUserId,
		@RequestParam(required = false) String sortBy,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) Integer limit
	) {
		log.info("댓글 목록 조회 API 호출 - articleId: {}, sortBy: {}, cursor: {}, limit: {}",
			articleId, sortBy, cursor, limit);
		return commentService.getComments(articleId, requestUserId, sortBy, cursor, limit);
	}
}
