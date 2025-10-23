package com.codeit.monew.article.exception;

import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;

public class ArticleException extends BusinessException {
	public ArticleException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ArticleException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
