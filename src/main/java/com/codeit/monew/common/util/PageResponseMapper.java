package com.codeit.monew.common.util;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Slice;

import com.codeit.monew.common.dto.CursorPageResponse;

@Mapper(componentModel = "spring")
public interface PageResponseMapper {

	default <T> CursorPageResponse<T> toCursorPageResponse(Slice<T> slice, Object nextCursor, Object nextAfter) {

		return CursorPageResponse.<T>builder()
			.content(slice.getContent())
			.nextCursor(nextCursor.toString())
			.nextAfter(nextAfter.toString())
			.size(slice.getSize())
			.totalElements(slice.getNumberOfElements())
			.hasNext(slice.hasNext())
			.build();
	}
}
