package com.codeit.monew.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	// User
	USER_ALREADY_EXIST("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
	USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	LOGIN_FAILED("로그인에 실패했습니다. 이메일 또는 비밀번호를 확인하세요.", HttpStatus.UNAUTHORIZED),
	USER_ALREADY_DELETED("이미 삭제된 사용자입니다.", HttpStatus.CONFLICT),
	USER_NOT_SOFT_DELETED("논리 삭제되지 않은 사용자입니다.", HttpStatus.BAD_REQUEST),

	// Article
	ARTICLE_NOT_FOUND("기사를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	ARTICLE_VIEW_ALREADY_EXIST("이미 존재하는 기사 뷰입니다.", HttpStatus.CONFLICT),

	// Interest
	SIMILAR_INTEREST_EXISTS("이미 유사한 이름의 관심사가 존재합니다.", HttpStatus.CONFLICT),
	ALREADY_SUBSCRIBED("이미 구독한 관심사입니다.", HttpStatus.CONFLICT),
	INTEREST_NOT_FOUND("관심사 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	SUBSCRIPTION_NOT_FOUND("구독 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	INVALID_INPUT_VALUE("유효하지 않은 입력 값입니다.", HttpStatus.BAD_REQUEST),

	// Comment
	COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	COMMENT_LIKE_ALREADY_EXISTS("이미 좋아요를 누른 댓글입니다.", HttpStatus.CONFLICT),
	COMMENT_LIKE_NOT_FOUND("좋아요를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

	// Common
	ARTICLE_RESTORE_FAILED("기사 복원에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	FORBIDDEN("권한이 없습니다.", HttpStatus.FORBIDDEN),
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	VALIDATION_ERROR("요청 데이터 유효성 검사에 실패했습니다."),
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
