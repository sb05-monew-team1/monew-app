package com.codeit.monew.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.monew.comment.domain.Comment;
import com.codeit.monew.comment.dto.CommentDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {

	@Mapping(target = "userNickname", source = "userNickname")
	@Mapping(target = "likedByMe", source = "likedByMe")
	CommentDto toDto(Comment comment, String userNickname, boolean likedByMe);
}
