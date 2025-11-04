package com.codeit.monew.notification.exception;

import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;

public class NotificationException extends BusinessException {
	public NotificationException(ErrorCode errorCode) { super(errorCode); }

	public NotificationException(ErrorCode errorCode, Throwable cause) { super(errorCode, cause); }
}
