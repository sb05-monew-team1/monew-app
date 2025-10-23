package com.codeit.monew.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//로그인 요청 DTO
//api/users/login (POST)
public record UserLoginRequest(
	@NotBlank(message = "이메일을 입력해 주세요.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email,

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	String password

	//로그인 성공시 "로그인이 완료되었습니다." 라는 문구가 뜸. (차후 추가..)
) {
}
