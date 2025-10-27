package com.codeit.monew.comment.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentRegisterRequest;
import com.codeit.monew.comment.dto.CommentUpdateRequest;
import com.codeit.monew.comment.service.CommentService;

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
	 * @param requestUserId 요청자 ID (헤더)
	 */
	@DeleteMapping("/{commentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void softDeleteComment(
		@PathVariable UUID commentId,
		@RequestHeader("Monew-Request-User-ID") UUID requestUserId
	) {
		log.info("댓글 논리 삭제 API 호출 - commentId: {}, requestUserId: {}", commentId, requestUserId);
		commentService.softDeleteComment(commentId, requestUserId);
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
}
