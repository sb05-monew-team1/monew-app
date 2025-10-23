package com.codeit.monew.user.exception;

import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;

public class UserException extends BusinessException {
	public UserException(ErrorCode errorCode) {
		super(errorCode);
	}

	public UserException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
