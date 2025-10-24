package com.codeit.monew.user.exception;

import com.codeit.monew.common.exception.ErrorCode;

public class UserNotFoundException extends UserException {
	public UserNotFoundException() {
		super(ErrorCode.USER_NOT_FOUND);
	}
}
