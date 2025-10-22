package com.codeit.monew.interest.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record InterestUpdateRequest(

	@NotNull(message = "키워드는 필수입니다.")
	List<String> keywords
) {
}
