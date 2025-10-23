package com.codeit.monew.common.dto;

import java.util.List;

public record CursorPageResponse<T>(
	List<T> content,
	String nextCursor,
	String nextAfter,
	int size,
	long totalElements,
	boolean hasNext
) {
}
