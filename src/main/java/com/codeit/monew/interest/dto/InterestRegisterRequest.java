package com.codeit.monew.interest.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InterestRegisterRequest(
	@NotBlank(message = "관심사 이름은 필수입니다.")
	String name,

	@NotNull(message = "키워드는 필수입니다.")
	List<@NotBlank String> keywords
) {
}
