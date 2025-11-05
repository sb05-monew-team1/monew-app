package com.codeit.monew.activity.mapper;

import org.mapstruct.Mapper;

import com.codeit.monew.activity.domain.UserActivity;
import com.codeit.monew.activity.dto.UserActivityDto;

@Mapper(componentModel = "spring")
public interface UserActivityMapper {

	UserActivityDto toUserActivityDto(UserActivity userActivity);
}
