package com.codeit.monew.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import javax.inject.Singleton;

//사용자 정보 수정 요청 DTO
//api/users/{userId} (PUT/PATCH)
public record UserUpdateRequest(
    //공백이 아닌 문자열 체크
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 1, max = 20, message = "닉네임을 입력해주세요. (최대 20자)")
    String nickname
) {
}
