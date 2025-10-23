package com.codeit.monew.common.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private final Instant timestamp;
	private final Map<String, Object> details;
	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.timestamp = Instant.now();
		this.details = new HashMap<>();
	}

	public BusinessException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
		this.timestamp = Instant.now();
		this.details = new HashMap<>();
	}

	public BusinessException addDetail(String key, Object value) {
		this.details.put(key, value);
		return this;
	}
}
