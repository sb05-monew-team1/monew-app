package com.codeit.monew.notification.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.codeit.monew.common.exception.ErrorCode;

class NotificationExceptionTest {

	@Test
	@DisplayName("기본 생성자에서 에러 코드를 확인할 수 있다")
	void constructorWithoutCause() {
		NotificationException exception = new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND);

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
	}

	@Test
	@DisplayName("원인 예외를 전달할 수 있다")
	void constructorWithCause() {
		Throwable cause = new IllegalArgumentException("invalid");

		NotificationException exception = new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND, cause);

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
		assertThat(exception.getCause()).isEqualTo(cause);
	}

	@Test
	@DisplayName("세분화된 알림 예외도 에러 코드를 유지한다")
	void notFoundExceptionExposesErrorCode() {
		NotificationNotFoundException exception = new NotificationNotFoundException();

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
	}
}
