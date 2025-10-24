package com.codeit.monew.user.mapper;

import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDto toUserDto(User user);
}
