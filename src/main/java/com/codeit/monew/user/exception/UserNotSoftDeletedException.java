package com.codeit.monew.user.exception;

import java.util.UUID;

import com.codeit.monew.common.exception.ErrorCode;

public class UserNotSoftDeletedException extends UserException {

	public UserNotSoftDeletedException(UUID userId) {
		super(ErrorCode.USER_NOT_SOFT_DELETED);
		this.addDetail("userId", userId.toString());
	}
}
