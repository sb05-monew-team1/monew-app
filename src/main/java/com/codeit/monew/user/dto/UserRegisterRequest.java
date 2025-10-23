package com.codeit.monew.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//회원가입
//api/users (POST)
public record UserRegisterRequest(
    //아이디(이메일)
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    //닉네임
    //프로토타입에는 최대 10자라 나와있지만, 스웨거 명세에는 최대 20자..
    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(min = 1, max = 20, message = "닉네임을 입력해 주세요 (최대 20자)")
    String nickname,

    //비밀번호
    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,20}$",
        message = "영문과 숫자, 특수문자를 포함해 6자 이상 입력해 주세요"
    )
    String password
) {
}
