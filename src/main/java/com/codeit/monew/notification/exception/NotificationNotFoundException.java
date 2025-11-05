package com.codeit.monew.notification.exception;

import com.codeit.monew.common.exception.ErrorCode;

@SuppressWarnings("java:S110")
public class NotificationNotFoundException extends NotificationException {
	public NotificationNotFoundException() {
		super(ErrorCode.NOTIFICATION_NOT_FOUND);
	}
}
