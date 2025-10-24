package com.codeit.monew.user.dto;

public record UserLoginResponse(
    String accessToken
    //refreshToken토큰 추가여부 추후에
) {
}
