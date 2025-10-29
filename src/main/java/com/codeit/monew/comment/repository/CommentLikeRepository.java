package com.codeit.monew.comment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.monew.comment.domain.Comment;
import com.codeit.monew.comment.domain.CommentLike;
import com.codeit.monew.user.domain.User;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

	/**
	 * 특정 댓글에 대한 특정 사용자의 좋아요 조회
	 * @param comment 댓글
	 * @param user 사용자
	 * @return 좋아요 정보
	 */
	Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

	/**
	 * 특정 댓글에 대한 특정 사용자의 좋아요 존재 여부 확인
	 * @param comment 댓글
	 * @param user 사용자
	 * @return 좋아요 존재 여부
	 */
	boolean existsByCommentAndUser(Comment comment, User user);

	/**
	 * 특정 댓글에 대한 특정 사용자의 좋아요 삭제
	 * @param comment 댓글
	 * @param user 사용자
	 */
	void deleteByCommentAndUser(Comment comment, User user);
}
