package com.codeit.monew.article.exception;

import com.codeit.monew.common.exception.ErrorCode;

public class ArticleViewAlreadyExistException extends ArticleException {
	public ArticleViewAlreadyExistException() {
		super(ErrorCode.ARTICLE_VIEW_ALREADY_EXIST);
	}
}
