package com.codeit.monew.user.exception;

import com.codeit.monew.common.exception.ErrorCode;

public class UserForbiddenException extends UserException {
  public UserForbiddenException(){
    super(ErrorCode.LOGIN_FAILED);
  }

  public UserForbiddenException(String resourceType, String resourceId){
    super(ErrorCode.LOGIN_FAILED);
    this.addDetail("resourceType", resourceType)
        .addDetail(resourceType, resourceId);
  }

}
