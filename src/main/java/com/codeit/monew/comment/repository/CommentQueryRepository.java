package com.codeit.monew.comment.repository;

import java.util.List;
import java.util.UUID;

import com.codeit.monew.comment.dto.CommentActivityDto;

public interface CommentQueryRepository {

	List<CommentActivityDto> searchRecentComments(UUID userId);
}
