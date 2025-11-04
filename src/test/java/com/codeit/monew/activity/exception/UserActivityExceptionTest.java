package com.codeit.monew.activity.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.codeit.monew.common.exception.ErrorCode;

class UserActivityExceptionTest {

	@Test
	@DisplayName("기본 생성자가 에러 코드를 유지한다")
	void constructorWithoutCauseSetsErrorCode() {
		UserActivityException exception = new UserActivityException(ErrorCode.USERACTIVITY_NOT_FOUND);

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USERACTIVITY_NOT_FOUND);
	}

	@Test
	@DisplayName("원인 예외를 전달할 수 있다")
	void constructorWithCauseSetsErrorCode() {
		Throwable cause = new IllegalStateException("cause");

		UserActivityException exception = new UserActivityException(ErrorCode.USER_NOT_FOUND, cause);

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
		assertThat(exception.getCause()).isEqualTo(cause);
	}

	@Test
	@DisplayName("세분화된 예외 생성자를 통해 에러 코드를 노출한다")
	void notFoundExceptionExposesErrorCode() {
		UserActivityNotFoundException exception = new UserActivityNotFoundException();

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USERACTIVITY_NOT_FOUND);
	}
}
