package com.codeit.monew.user.exception;

import com.codeit.monew.common.exception.ErrorCode;

public class UserAlreadyExistsException extends UserException {

  public UserAlreadyExistsException(String email) {
    super(ErrorCode.USER_ALREADY_EXIST);
    this.addDetail("email", email);
  }
}
