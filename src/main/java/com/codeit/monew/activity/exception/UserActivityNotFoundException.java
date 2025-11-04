package com.codeit.monew.activity.exception;

import com.codeit.monew.common.exception.ErrorCode;

@SuppressWarnings("java:S110")
public class UserActivityNotFoundException extends UserActivityException {
	public UserActivityNotFoundException() {
		super(ErrorCode.USERACTIVITY_NOT_FOUND);
	}

}
