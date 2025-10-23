package com.codeit.monew.user.dto;

import java.time.Instant;
import java.util.UUID;

//사용자 정보 응답 DTO
public record UserDto(
    UUID id,
    String email,
    String nickname,
    Instant createdAt
) {
}
