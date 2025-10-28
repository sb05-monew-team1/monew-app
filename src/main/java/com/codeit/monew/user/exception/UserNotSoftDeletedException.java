package com.codeit.monew.user.exception;

import com.codeit.monew.common.exception.ErrorCode;
import java.util.UUID;

public class UserNotSoftDeletedException extends UserException {

  public UserNotSoftDeletedException(UUID userId) {
    super(ErrorCode.USER_NOT_SOFT_DELETED);
    this.addDetail("userId", userId.toString());
  }
}
