package com.codeit.monew.comment.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.comment.domain.Comment;
import com.querydsl.core.types.Order;

/**
 * 댓글 Repository
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

	/**
	 * 특정 기사의 논리 삭제되지 않은 댓글 목록 조회 (날짜순 내림차순, 커서 페이지네이션)
	 */
	@Query("SELECT c FROM Comment c WHERE c.article = :article AND c.deletedAt IS NULL " +
		"AND (:cursor IS NULL OR c.createdAt < :cursor) " +
		"ORDER BY c.createdAt DESC")
	Slice<Comment> findByArticleAndNotDeletedOrderByDateDesc(
		@Param("article") Article article,
		@Param("cursor") Instant cursor,
		Pageable pageable
	);

	/**
	 * 특정 기사의 논리 삭제되지 않은 댓글 목록 조회 (날짜순 오름차순, 커서 페이지네이션)
	 */
	@Query("SELECT c FROM Comment c WHERE c.article = :article AND c.deletedAt IS NULL " +
		"AND (:cursor IS NULL OR c.createdAt > :cursor) " +
		"ORDER BY c.createdAt ASC")
	Slice<Comment> findByArticleAndNotDeletedOrderByDateAsc(
		@Param("article") Article article,
		@Param("cursor") Instant cursor,
		Pageable pageable
	);

	/**
	 * 특정 기사의 논리 삭제되지 않은 댓글 목록 조회 (좋아요순 내림차순, 커서 페이지네이션)
	 */
	@Query("SELECT c FROM Comment c WHERE c.article = :article AND c.deletedAt IS NULL " +
		"AND (:likeCursor IS NULL OR c.likeCount < :likeCursor " +
		"OR (c.likeCount = :likeCursor AND c.createdAt < :dateCursor)) " +
		"ORDER BY c.likeCount DESC, c.createdAt DESC")
	Slice<Comment> findByArticleAndNotDeletedOrderByLikesDesc(
		@Param("article") Article article,
		@Param("likeCursor") Long likeCursor,
		@Param("dateCursor") Instant dateCursor,
		Pageable pageable
	);

	/**
	 * 특정 기사의 논리 삭제되지 않은 댓글 목록 조회 (좋아요순 오름차순, 커서 페이지네이션)
	 */
	@Query("SELECT c FROM Comment c WHERE c.article = :article AND c.deletedAt IS NULL " +
		"AND (:likeCursor IS NULL OR c.likeCount > :likeCursor " +
		"OR (c.likeCount = :likeCursor AND c.createdAt > :dateCursor)) " +
		"ORDER BY c.likeCount ASC, c.createdAt ASC")
	Slice<Comment> findByArticleAndNotDeletedOrderByLikesAsc(
		@Param("article") Article article,
		@Param("likeCursor") Long likeCursor,
		@Param("dateCursor") Instant dateCursor,
		Pageable pageable
	);
}
