package com.codeit.monew.user.exception;

import com.codeit.monew.common.exception.ErrorCode;
import java.util.UUID;

public class UserAlreadyDeletedException extends UserException {

  // ID를 detail에 추가하여 로깅/응답에 활용
  public UserAlreadyDeletedException(UUID userId) {
    super(ErrorCode.USER_ALREADY_DELETED);
    this.addDetail("userId", userId.toString());
  }

}