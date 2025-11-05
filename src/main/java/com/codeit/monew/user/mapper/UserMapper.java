package com.codeit.monew.user.mapper;

import org.mapstruct.Mapper;

import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.dto.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {
	UserDto toUserDto(User user);
}
