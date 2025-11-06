package com.codeit.monew.common.exception.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.codeit.monew.common.exception.ErrorCode;

class StorageExceptionTest {

	@Test
	void storesErrorCode() {
		StorageException exception = new StorageException(ErrorCode.INVALID_INPUT_VALUE);

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
	}

	@Test
	void storesCauseWhenProvided() {
		IllegalStateException cause = new IllegalStateException("boom");
		StorageException exception = new StorageException(ErrorCode.INTERNAL_SERVER_ERROR, cause);

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
		assertThat(exception.getCause()).isSameAs(cause);
	}
}
