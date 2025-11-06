package com.codeit.monew.comment.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class CommentLikeRequestTest {

	@Test
	void exposesUserId() {
		UUID userId = UUID.randomUUID();

		CommentLikeRequest request = new CommentLikeRequest(userId);

		assertThat(request.userId()).isEqualTo(userId);
	}
}
