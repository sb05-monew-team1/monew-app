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
	// 직렬화 버전 UID (레코드에도 추가 가능)
	private static final long serialVersionUID = 1L;
}
