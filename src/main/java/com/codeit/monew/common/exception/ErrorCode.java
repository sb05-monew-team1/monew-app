package com.codeit.monew.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	SIMILAR_INTEREST_EXISTS("이미 유사한 이름의 관심사가 존재합니다.", HttpStatus.CONFLICT),
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	;

	private final String message;
	private final HttpStatus status;

	ErrorCode() {
		this.message = this.name();
		this.status = HttpStatus.BAD_REQUEST;
	}

	ErrorCode(String message) {
		this.message = message;
		this.status = HttpStatus.BAD_REQUEST;
	}

	ErrorCode(HttpStatus status) {
		this.message = this.name();
		this.status = status;
	}
}
