package com.codeit.monew.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void registerCreatesUserWithProvidedValues() {
		User user = User.register("user@example.com", "nickname", "encodedPwd");

		assertThat(user.getEmail()).isEqualTo("user@example.com");
		assertThat(user.getNickname()).isEqualTo("nickname");
		assertThat(user.getPassword()).isEqualTo("encodedPwd");
	}

	@Test
	void updateNicknameChangesNickname() {
		User user = User.register("user@example.com", "nickname", "encodedPwd");

		user.updateNickname("newNickname");

		assertThat(user.getNickname()).isEqualTo("newNickname");
	}

	@Test
	void softDeleteSetsDeletedAt() {
		User user = User.register("user@example.com", "nickname", "encodedPwd");

		user.softDelete();

		assertThat(user.getDeletedAt()).isNotNull();
	}
}
