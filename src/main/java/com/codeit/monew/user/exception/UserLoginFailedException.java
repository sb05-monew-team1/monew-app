package com.codeit.monew.user.exception;

import com.codeit.monew.common.exception.ErrorCode;

public class UserLoginFailedException extends UserException {
  public UserLoginFailedException() {
    super(ErrorCode.LOGIN_FAILED);
  }

  //실패한 이메일 정보 보여줌..(테스트 후 남김여부 결정)
  public UserLoginFailedException(String email) {
    super(ErrorCode.LOGIN_FAILED);
    this.addDetail("email", email);
  }
}
