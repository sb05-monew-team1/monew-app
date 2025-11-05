package com.codeit.monew.comment.repository;

import java.util.List;
import java.util.UUID;

import com.codeit.monew.comment.dto.CommentLikeActivityDto;

public interface CommentLikeQueryRepository {

	List<CommentLikeActivityDto> searchRecentCommentLikes(UUID userId);
}
