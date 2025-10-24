package com.codeit.monew.article.exception;

import com.codeit.monew.common.exception.ErrorCode;

public class ArticleNotFoundException extends ArticleException {
	public ArticleNotFoundException() {
		super(ErrorCode.ARTICLE_NOT_FOUND);
	}
}
