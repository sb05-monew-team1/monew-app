package com.codeit.monew.interest.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterestSearchRequestTest {

	@Test
	@DisplayName("커서 문자열에서 after 값이 파싱된다")
	void parseCursor_SplitsCursor() {
		InterestSearchRequest request = new InterestSearchRequest(
			null,
			"name",
			"ASC",
			"foo|2024-01-01T00:00:00Z",
			null,
			10
		);

		InterestSearchRequest result = InterestSearchRequest.parseCursor(request);

		assertEquals("foo", result.cursor());
		assertEquals("2024-01-01T00:00:00Z", result.after());
	}

	@Test
	@DisplayName("after 가 이미 존재하면 커서 문자열을 분해하지 않는다")
	void parseCursor_KeepsProvidedAfter() {
		InterestSearchRequest request = new InterestSearchRequest(
			null,
			"name",
			"ASC",
			"foo|2024-01-01T00:00:00Z",
			"2023-12-31T00:00:00Z",
			10
		);

		InterestSearchRequest result = InterestSearchRequest.parseCursor(request);

		assertEquals("foo|2024-01-01T00:00:00Z", result.cursor());
		assertEquals("2023-12-31T00:00:00Z", result.after());
	}

	@Test
	@DisplayName("커서 문자열에 구분자가 없으면 원본 값이 유지된다")
	void parseCursor_NoDelimiter() {
		InterestSearchRequest request = new InterestSearchRequest(
			null,
			"name",
			"ASC",
			"foo",
			null,
			10
		);

		InterestSearchRequest result = InterestSearchRequest.parseCursor(request);

		assertEquals("foo", result.cursor());
		assertNull(result.after());
	}
}
