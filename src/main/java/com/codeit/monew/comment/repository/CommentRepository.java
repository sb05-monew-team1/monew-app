package com.codeit.monew.comment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.monew.comment.domain.Comment;

/**
 * 댓글 Repository
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
