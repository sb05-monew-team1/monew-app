package com.codeit.monew.user.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.codeit.monew.common.exception.ErrorCode;

class UserExceptionsTest {

	@Test
	void userAlreadyDeletedExceptionAddsDetail() {
		UUID userId = UUID.randomUUID();

		UserAlreadyDeletedException exception = new UserAlreadyDeletedException(userId);

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_DELETED);
		assertThat(exception.getDetails()).containsEntry("userId", userId.toString());
	}

	@Test
	void userAlreadyExistsExceptionAddsEmailDetail() {
		UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_EXIST);
		assertThat(exception.getDetails()).containsEntry("email", "test@example.com");
	}

	@Test
	void userForbiddenExceptionAllowsResourceDetail() {
		UserForbiddenException exception = new UserForbiddenException("comment", "123");

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED);
		assertThat(exception.getDetails()).containsEntry("resourceType", "comment")
			.containsEntry("comment", "123");
	}

	@Test
	void userLoginFailedExceptionCanIncludeEmail() {
		UserLoginFailedException exception = new UserLoginFailedException("fail@example.com");

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED);
		assertThat(exception.getDetails()).containsEntry("email", "fail@example.com");
	}

	@Test
	void userNotSoftDeletedExceptionAddsUserIdDetail() {
		UUID userId = UUID.randomUUID();

		UserNotSoftDeletedException exception = new UserNotSoftDeletedException(userId);

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_SOFT_DELETED);
		assertThat(exception.getDetails()).containsEntry("userId", userId.toString());
	}
}
