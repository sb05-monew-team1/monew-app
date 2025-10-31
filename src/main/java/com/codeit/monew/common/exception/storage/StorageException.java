package com.codeit.monew.common.exception.storage;

import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;

public class StorageException extends BusinessException {
	public StorageException(ErrorCode code) {
		super(code);
	}

	public StorageException(ErrorCode code, Throwable cause) {
		super(code, cause);
	}
}
