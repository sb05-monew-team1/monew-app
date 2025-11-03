package com.codeit.monew.activity.exception;

import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;

public class UserActivityException extends BusinessException {
	public UserActivityException(ErrorCode errorCode) { super(errorCode); }

	public UserActivityException(ErrorCode errorCode, Throwable cause) { super(errorCode, cause); }
}
