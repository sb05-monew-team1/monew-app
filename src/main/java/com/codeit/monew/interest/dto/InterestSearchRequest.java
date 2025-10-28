package com.codeit.monew.interest.dto;

import java.time.Instant;

import com.codeit.monew.interest.domain.InterestOrder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InterestSearchRequest(
	String keyword,

	@NotNull(message = "정렬 속성은 필수입니다.")
	InterestOrder orderBy,

	@NotBlank(message = "정렬 방향은 필수입니다.")
	String direction,

	String cursor,
	Instant after,

	@NotNull(message = "페이지 크기는 필수입니다.")
	Integer limit
) {
}
