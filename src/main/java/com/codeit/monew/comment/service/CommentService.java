package com.codeit.monew.comment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.domain.Comment;
import com.codeit.monew.comment.domain.CommentLike;
import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentRegisterRequest;
import com.codeit.monew.comment.dto.CommentUpdateRequest;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 댓글 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

	private final CommentRepository commentRepository;
	private final CommentLikeRepository commentLikeRepository;
	private final UserRepository userRepository;
	private final ArticleRepository articleRepository;
	private final CommentMapper commentMapper;

	/**
	 * 댓글 등록
	 * @param request 댓글 등록 요청
	 * @return 등록된 댓글 정보
	 */
	//jpa 매핑
	@Transactional
	public CommentDto registerComment(CommentRegisterRequest request) {
		log.info("댓글 등록 시작 - articleId: {}, userId: {}", request.articleId(), request.userId());

		// 기사 존재 확인 (수정완)
		Article article = articleRepository.findById(request.articleId())
			.orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND)
				.addDetail("articleId", request.articleId()));

		// 사용자 존재 확인
		User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)
				.addDetail("userId", request.userId()));

		// Comment 엔티티 생성 (JPA 매핑)
		Comment comment = Comment.builder()
			.article(article)    // Article 객체 직접 할당
			.user(user)          // User 객체 직접 할당
			.content(request.content())
			.likeCount(0L)
			.build();

		// 저장
		Comment savedComment = commentRepository.save(comment);

		log.info("댓글 등록 완료 - commentId: {}", savedComment.getId());

		// DTO 변환 후 반환 (JPA 매핑으로 user.getNickname() 바로 접근 가능)
		return commentMapper.toDto(savedComment, savedComment.getUser().getNickname(), false);
	}

	/**
	 * 댓글 수정
	 * @param commentId 댓글 ID
	 * @param requestUserId 요청자 ID
	 * @param request 댓글 수정 요청
	 * @return 수정된 댓글 정보
	 */
	@Transactional
	public CommentDto updateComment(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
		log.info("댓글 수정 시작 - commentId: {}, requestUserId: {}", commentId, requestUserId);

		// 댓글 조회
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND)
				.addDetail("commentId", commentId));

		// 권한 확인 (본인이 작성한 댓글인지)
		if (!comment.getUser().getId().equals(requestUserId)) {
			log.warn("댓글 수정 권한 없음 - commentId: {}, commentUserId: {}, requestUserId: {}",
				commentId, comment.getUser().getId(), requestUserId);
			throw new BusinessException(ErrorCode.FORBIDDEN)
				.addDetail("commentId", commentId)
				.addDetail("requestUserId", requestUserId);
		}

		// 내용 수정
		comment.updateContent(request.content());

		log.info("댓글 수정 완료 - commentId: {}", commentId);

		// DTO 변환 후 반환
		return commentMapper.toDto(comment, comment.getUser().getNickname(), false);
	}

	/**
	 * 댓글 논리 삭제
	 * @param commentId 댓글 ID
	 */
	@Transactional
	public void softDeleteComment(UUID commentId) {
		log.info("댓글 논리 삭제 시작 - commentId: {}", commentId);

		// 댓글 조회
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND)
				.addDetail("commentId", commentId));

		// 논리 삭제 (deletedAt 필드에 현재 시간 저장)
		comment.softDelete();

		log.info("댓글 논리 삭제 완료 - commentId: {}", commentId);
	}

	/**
	 * 댓글 물리 삭제 (테스트용)
	 * @param commentId 댓글 ID
	 */
	@Transactional
	public void hardDeleteComment(UUID commentId) {
		log.info("댓글 물리 삭제 시작 - commentId: {}", commentId);

		// 댓글 조회
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND)
				.addDetail("commentId", commentId));

		// DB에서 완전 삭제
		commentRepository.delete(comment);

		log.info("댓글 물리 삭제 완료 - commentId: {}", commentId);
	}

	/**
	 * 댓글 좋아요 등록
	 * @param commentId 댓글 ID
	 * @param userId 사용자 ID
	 */
	@Transactional
	public void likeComment(UUID commentId, UUID userId) {
		log.info("댓글 좋아요 등록 시작 - commentId: {}, userId: {}", commentId, userId);

		// 댓글 조회
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND)
				.addDetail("commentId", commentId));

		// 사용자 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)
				.addDetail("userId", userId));

		// CommentLike 생성 및 저장
		CommentLike commentLike = CommentLike.builder()
			.comment(comment)
			.user(user)
			.build();
		commentLikeRepository.save(commentLike);

		// 댓글의 좋아요 수 증가
		comment.increaseLikeCount();

		log.info("댓글 좋아요 등록 완료 - commentId: {}, userId: {}", commentId, userId);
	}

	/**
	 * 댓글 좋아요 취소
	 * @param commentId 댓글 ID
	 * @param userId 사용자 ID
	 */
	@Transactional
	public void unlikeComment(UUID commentId, UUID userId) {
		log.info("댓글 좋아요 취소 시작 - commentId: {}, userId: {}", commentId, userId);

		// 댓글 조회
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND)
				.addDetail("commentId", commentId));

		// 사용자 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)
				.addDetail("userId", userId));

		// 좋아요 기록 조회
		CommentLike commentLike = commentLikeRepository.findByCommentAndUser(comment, user)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_LIKE_NOT_FOUND)
				.addDetail("commentId", commentId)
				.addDetail("userId", userId));

		// CommentLike 삭제
		commentLikeRepository.delete(commentLike);

		// 댓글의 좋아요 수 감소
		comment.decreaseLikeCount();

		log.info("댓글 좋아요 취소 완료 - commentId: {}, userId: {}", commentId, userId);
	}
}
