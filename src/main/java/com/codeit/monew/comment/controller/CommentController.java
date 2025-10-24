package com.codeit.monew.comment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentRegisterRequest;
import com.codeit.monew.comment.service.CommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
}
