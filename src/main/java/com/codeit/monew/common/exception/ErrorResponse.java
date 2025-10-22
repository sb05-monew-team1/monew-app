package com.codeit.monew.common.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
	private final String code;
	private final String message;

	public ErrorResponse(ErrorCode errorCode) {
		this.code = errorCode.name();
		this.message = errorCode.getMessage();
	}
}
