package com.codeit.monew.comment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.domain.Comment;
import com.codeit.monew.comment.dto.CommentDto;
import com.codeit.monew.comment.dto.CommentRegisterRequest;
import com.codeit.monew.comment.mapper.CommentMapper;
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

		// 기사 존재 확인
		Article article = articleRepository.findById(request.articleId())
			.orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

		// 사용자 존재 확인
		User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
}
