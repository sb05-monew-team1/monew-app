package com.codeit.monew.user.dto;

import java.time.LocalDateTime;

//사용자 정보 응답 DTO
public record UserDto(
    Long userId,
    String email,
    String nickname,
    LocalDateTime createdAt,
    LocalDateTime lastModifiedAt
) {
}
