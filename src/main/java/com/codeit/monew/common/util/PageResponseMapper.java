package com.codeit.monew.common.util;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Slice;

import com.codeit.monew.common.dto.CursorPageResponse;

@Mapper(componentModel = "spring")
public interface PageResponseMapper {

	default <T> CursorPageResponse<T> toCursorPageResponse(Slice<T> slice, Object nextCursor, Object nextAfter,
		long totalElements) {

		return CursorPageResponse.<T>builder()
			.content(slice.getContent())
			.nextCursor(nextCursor == null ? null : nextCursor.toString())
			.nextAfter(nextAfter == null ? null : nextAfter.toString())
			.size(slice.getSize())
			.totalElements(totalElements)
			.hasNext(slice.hasNext())
			.build();
	}
}
