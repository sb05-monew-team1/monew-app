package com.codeit.monew.user.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

//사용자 정보 응답 DTO
public record UserDto(
	UUID id,
	String email,
	String nickname,
	Instant createdAt
) implements Serializable { //직렬화 추가(가독성)
}
